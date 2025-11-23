package com.team.mediguide.ui.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ServerException;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.team.mediguide.BuildConfig;
import com.team.mediguide.CartItem;
import com.team.mediguide.ChatMessage;
import com.team.mediguide.ChatMessageAdapter;
import com.team.mediguide.Product;
import com.team.mediguide.R;

import com.google.firebase.auth.FirebaseAuth; // Get the currently logged in user
import com.google.firebase.auth.FirebaseUser; // Get user information
import com.google.firebase.firestore.DocumentSnapshot; // Ability to read individual documents from Firestore
import com.google.firebase.firestore.FirebaseFirestore; // To query the database for cart items and products

import java.util.ArrayList;
import java.util.List;

public class AiFragment extends Fragment {

    private static final String TAG = "AiFragment";

    private AiViewModel viewModel;  // ViewModel to persist data across lifecycle
    private RecyclerView chatRecyclerView;
    private ChatMessageAdapter chatAdapter;
    private List<ChatMessage> chatMessages; // Reference to ViewModel's list
    private List<Content> chatHistory; // Reference to ViewModel's list
    private EditText chatInput;
    private Button sendButton;
    private android.widget.TextView newChatText;  // New Chat text link
    private GenerativeModelFutures generativeModel;
    private FirebaseFirestore db;              // Firestore database instance
    private List<Product> cartProducts;        // Products currently in user's cart
    private String cartContext = "";           // Formatted cart info for AI context

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ai, container, false);

        // Initialize ViewModel (survives fragment recreation)
        viewModel = new ViewModelProvider(this).get(AiViewModel.class);

        // Get references to ViewModel's lists (persisted across lifecycle)
        chatMessages = viewModel.getChatMessages();
        chatHistory = viewModel.getChatHistory();

        chatRecyclerView = root.findViewById(R.id.chat_recyclerview);
        chatInput = root.findViewById(R.id.chat_input);
        sendButton = root.findViewById(R.id.send_button);
        newChatText = root.findViewById(R.id.new_chat_text);

        chatAdapter = new ChatMessageAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        GenerativeModel gm = new GenerativeModel("gemini-2.5-flash", BuildConfig.MEDI_GUIDE_API_KEY);
        generativeModel = GenerativeModelFutures.from(gm);

        db = FirebaseFirestore.getInstance(); // Initializes the connection to the Firestore database

        sendButton.setOnClickListener(v -> {
            String messageText = chatInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });

        newChatText.setOnClickListener(v -> resetConversation());

        // Only load cart context if this is the first time (history is empty)
        if (chatHistory.isEmpty()) {
            loadCartContext(); // Load the user's cart context
        }
        listenForCartUpdates(); // Listen for changes to the user's cart

        return root;
    }

    private void sendMessage(String messageText) {
        // Add user message to UI and history
        chatMessages.add(new ChatMessage(messageText, true));
        chatHistory.add(new Content.Builder().addText(messageText).build());
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatInput.setText("");

        // Add loading indicator
        chatMessages.add(new ChatMessage("...", false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        
        // Convert the history to the required array format
        Content[] historyArray = chatHistory.toArray(new Content[0]);

        // Send the entire history to the model
        ListenableFuture<GenerateContentResponse> response = generativeModel.generateContent(historyArray);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(@NonNull GenerateContentResponse result) {
                String resultText = result.getText();
                // Add model response to history
                chatHistory.add(result.getCandidates().get(0).getContent());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // FIX: Update the item in place instead of remove/add
                        int loadingIndicatorPosition = chatMessages.size() - 1;
                        if (loadingIndicatorPosition >= 0) {
                            chatMessages.set(loadingIndicatorPosition, new ChatMessage(resultText, false));
                            chatAdapter.notifyItemChanged(loadingIndicatorPosition);
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // FIX: Update the item in place with an error message
                        int loadingIndicatorPosition = chatMessages.size() - 1;
                        if (loadingIndicatorPosition >= 0) {
                            String errorMessage;
                            if (t instanceof ServerException) {
                                errorMessage = "Sorry, the AI is currently overloaded. Please try again later.";
                            } else {
                                errorMessage = "An error occurred. Please try again.";
                            }
                            chatMessages.set(loadingIndicatorPosition, new ChatMessage(errorMessage, false));
                            chatAdapter.notifyItemChanged(loadingIndicatorPosition);
                        }
                    });
                }
                Log.e(TAG, "Error generating AI response", t);
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    // ==================== CART CONTEXT METHODS ====================
    // These methods handle loading cart data and providing it to the AI

    /**
     * Loads the user's cart items from Firestore
     * 
     * This is the entry point for the cart loading process. It:
     * 1. Checks if user is logged in
     * 2. Fetches cart items from Firestore
     * 3. Triggers product detail fetching
     * 4. Handles empty cart and error cases
     */
    private void loadCartContext() {
        // Get the currently logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Handle case where user is not logged in
        if (currentUser == null) {
            cartContext = "The user is not logged in, so no cart information is available.";
            updateSystemInstruction();
            return;
        }

        String userId = currentUser.getUid();

        // Query Firestore for user's cart items
        // Path: ShoppingCarts/{userId}/Items
        db.collection("ShoppingCarts").document(userId).collection("Items")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Handle empty cart
                if (querySnapshot.isEmpty()) {
                    cartContext = "The user's cart is currently empty.";
                    updateSystemInstruction();
                    return;
                }

                // Convert Firestore documents to CartItem objects
                List<CartItem> items = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    CartItem item = doc.toObject(CartItem.class);
                    if (item != null) {
                        item.id = doc.getId();
                        items.add(item);
                    }
                }

                // Now fetch full product details for each cart item
                fetchProductDetails(items);
            })
            .addOnFailureListener(e -> {
                // Handle database errors
                cartContext = "Unable to load cart information.";
                updateSystemInstruction();
                Log.e(TAG, "Error loading cart context", e);
            });
    }

    /**
     * Fetches full product details for all cart items
     * 
     * Cart items only contain Product_ID and Quantity. This method fetches
     * the full product information (name, ingredients, dosage, etc.) from
     * the Products collection.
     * 
     * Uses Firestore's whereIn query for efficient batch fetching (1 query
     * for up to 10 products instead of 10 separate queries).
     * 
     * @param cartItems List of cart items to fetch products for
     */
    private void fetchProductDetails(List<CartItem> cartItems) {
        // Extract all product IDs from cart items
        List<String> productIds = new ArrayList<>();
        for (CartItem item : cartItems) {
            productIds.add(item.productId);
        }

        // Firestore's whereIn supports up to 10 items at a time
        // If cart has 10 or fewer items, fetch in one query
        if (productIds.size() <= 10) {
            fetchProductBatch(productIds, cartItems);
        } else {
            // If more than 10 items, we need to fetch in multiple batches
            cartProducts = new ArrayList<>();
            fetchProductBatches(productIds, cartItems, 0);
        }
    }

    /**
     * Fetches a single batch of products (up to 10)
     * 
     * @param productIds List of product IDs to fetch (max 10)
     * @param cartItems Original cart items (needed for quantity info)
     */
    private void fetchProductBatch(List<String> productIds, List<CartItem> cartItems) {
        db.collection("Products")
            // Query for all products whose document ID is in our list
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), productIds)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Convert Firestore documents to Product objects
                cartProducts = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Product product = doc.toObject(Product.class);
                    if (product != null) {
                        product.id = doc.getId();
                        cartProducts.add(product);
                    }
                }
                // Now build the formatted context string
                buildCartContext(cartItems);
            })
            .addOnFailureListener(e -> {
                cartContext = "Unable to load product information.";
                updateSystemInstruction();
                Log.e(TAG, "Error fetching product batch", e);
            });
    }

    /**
     * Recursively fetches products in batches of 10
     * 
     * This handles carts with more than 10 items by making multiple
     * whereIn queries (Firestore limitation).
     * 
     * @param allProductIds Complete list of product IDs
     * @param cartItems Original cart items
     * @param batchIndex Current batch number (0-indexed)
     */
    private void fetchProductBatches(List<String> allProductIds, List<CartItem> cartItems, int batchIndex) {
        // Calculate which slice of product IDs to fetch in this batch
        int startIndex = batchIndex * 10;
        int endIndex = Math.min(startIndex + 10, allProductIds.size());

        // If we've fetched all batches, build the context
        if (startIndex >= allProductIds.size()) {
            buildCartContext(cartItems);
            return;
        }

        // Get the next batch of up to 10 product IDs
        List<String> batchIds = allProductIds.subList(startIndex, endIndex);
        
        db.collection("Products")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batchIds)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Add products from this batch to our list
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Product product = doc.toObject(Product.class);
                    if (product != null) {
                        product.id = doc.getId();
                        cartProducts.add(product);
                    }
                }
                // Recursively fetch the next batch
                fetchProductBatches(allProductIds, cartItems, batchIndex + 1);
            })
            .addOnFailureListener(e -> {
                cartContext = "Unable to load product information.";
                updateSystemInstruction();
                Log.e(TAG, "Error fetching product batches", e);
            });
    }

    /**
     * Builds a formatted string containing cart information for the AI
     * 
     * Creates a human-readable summary of cart contents including:
     * - Product name and quantity
     * - Ingredients
     * - Recommended usage/dosage
     * - Description
     * 
     * Example output:
     * Current Cart Contents:
     * 1. Aspirin 500mg (Quantity: 2)
     *    - Ingredients: Acetylsalicylic acid
     *    - Recommended Usage: Take 1-2 tablets every 4-6 hours
     * 
     * @param cartItems List of cart items (contains quantity info)
     */
    private void buildCartContext(List<CartItem> cartItems) {
        StringBuilder context = new StringBuilder("\n\nCurrent Cart Contents:\n");

        // Loop through each product and format its information
        for (int i = 0; i < cartProducts.size(); i++) {
            Product product = cartProducts.get(i);
            CartItem cartItem = findCartItemForProduct(product.id, cartItems);

            // Add product name and quantity
            context.append(String.format(java.util.Locale.getDefault(), "%d. %s (Quantity: %d)\n", 
                i + 1, product.name, cartItem != null ? cartItem.quantity : 0));

            // Add ingredients if available
            if (product.ingredients != null && !product.ingredients.isEmpty()) {
                context.append("   - Ingredients: ").append(product.ingredients).append("\n");
            }

            // Add recommended usage if available
            if (product.recommendedUsage != null && !product.recommendedUsage.isEmpty()) {
                context.append("   - Recommended Usage: ").append(product.recommendedUsage).append("\n");
            }

            // Add description if available
            if (product.description != null && !product.description.isEmpty()) {
                context.append("   - Description: ").append(product.description).append("\n");
            }

            context.append("\n");
        }

        // Store the formatted context
        cartContext = context.toString();

        // Update the AI's system instruction with this context
        updateSystemInstruction();
    }

    /**
     * Helper method to find the cart item for a specific product
     * 
     * We have two separate lists:
     * - cartProducts: Full product details (name, ingredients, etc.)
     * - cartItems: Cart-specific info (quantity, date added)
     * 
     * This method matches them up by product ID.
     * 
     * @param productId The product ID to search for
     * @param cartItems List of cart items to search
     * @return The matching CartItem, or null if not found
     */
    private CartItem findCartItemForProduct(String productId, List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            if (item.productId.equals(productId)) {
                return item;
            }
        }
        return null; // Not found (shouldn't happen in normal operation)
    }

    /**
     * Updates the AI's system instruction with cart context
     * 
     * The system instruction tells the AI:
     * 1. What its role is (helpful MediGuide assistant)
     * 2. What information it has access to (cart contents)
     * 
     * This is called after cart data is loaded, and can be called again
     * if cart changes (when using real-time updates).
     */
    private void updateSystemInstruction() {
        // Base instruction defining AI's role and behavior
        String baseInstruction = "You are MediGuide's friendly and helpful AI assistant. " +
            "Your goal is to help users with questions about their health and the products we sell. " +
            "Keep your answers concise and clear." +
            "Try to avoid using ** when printing messages";

        // Combine base instruction with cart context
        // This gives the AI knowledge of what's in the user's cart
        String fullInstruction = baseInstruction + cartContext;

        // Clear existing chat history and create new system instruction
        if (chatHistory.isEmpty()) {
            // First time - add system instruction
            Content systemInstruction = new Content.Builder()
                    .addText(fullInstruction)
                    .build();
            chatHistory.add(systemInstruction);
        } else {
            // Update existing system instruction (always at index 0)
            Content systemInstruction = new Content.Builder()
                    .addText(fullInstruction)
                    .build();
            chatHistory.set(0, systemInstruction);  // ✅ Replace only first item
        }
    }

    private void listenForCartUpdates() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Set up real-time listener on user's cart
            db.collection("ShoppingCarts").document(userId).collection("Items")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return; // Handle error silently
                    }
                    // Cart changed - reload context
                    loadCartContext();
                });
        }
    }

    /**
     * Resets the conversation to start fresh
     * 
     * Clears all chat messages from the UI and conversation history,
     * then reloads the cart context to start a new conversation.
     */
    private void resetConversation() {
        // Clear UI messages
        chatMessages.clear();
        chatAdapter.notifyDataSetChanged();

        // Clear conversation history
        chatHistory.clear();

        // Reload cart context (will add fresh system instruction)
        loadCartContext();

        // Optional: Show confirmation to user
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), "New conversation started", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}

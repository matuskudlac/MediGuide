package com.team.mediguide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity {

    private RecyclerView checkoutItemsRecycler;
    private CheckoutItemAdapter adapter;
    private List<OrderItem> orderItems;
    private TextView subtotalText, taxText, totalText;
    private Button payButton;
    
    private com.google.android.material.textfield.TextInputEditText streetInput, cityInput, zipInput, countryInput;
    private AutoCompleteTextView stateInput;
    
    private double subtotal = 0.0;
    private double tax = 0.0;
    private double total = 0.0;
    
    private FirebaseFirestore db;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        
        db = FirebaseFirestore.getInstance();
        orderId = UUID.randomUUID().toString();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.checkout_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        checkoutItemsRecycler = findViewById(R.id.checkout_items_recycler);
        subtotalText = findViewById(R.id.checkout_subtotal);
        taxText = findViewById(R.id.checkout_tax);
        totalText = findViewById(R.id.checkout_total);
        payButton = findViewById(R.id.pay_button);
        
        // Initialize shipping address inputs
        streetInput = findViewById(R.id.input_street);
        cityInput = findViewById(R.id.input_city);
        stateInput = findViewById(R.id.input_state);
        zipInput = findViewById(R.id.input_zip);
        countryInput = findViewById(R.id.input_country);
        
        // Setup state dropdown
        setupStateDropdown();

        // Get order items from intent
        //noinspection unchecked
        orderItems = (List<OrderItem>) getIntent().getSerializableExtra("ORDER_ITEMS");
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }

        // Setup RecyclerView
        checkoutItemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckoutItemAdapter(orderItems);
        checkoutItemsRecycler.setAdapter(adapter);

        // Calculate totals
        calculateTotals();

        // Setup pay button
        payButton.setOnClickListener(v -> presentPaymentSheet());
    }
    
    private void setupStateDropdown() {
        String[] states = new String[]{
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, states);
        stateInput.setAdapter(adapter);
    }

    private void calculateTotals() {
        subtotal = 0.0;
        for (OrderItem item : orderItems) {
            subtotal += item.getLineTotal();
        }
        
        tax = subtotal * 0.08; // 8% tax
        total = subtotal + tax;

        subtotalText.setText(String.format(java.util.Locale.US, "$%.2f", subtotal));
        taxText.setText(String.format(java.util.Locale.US, "$%.2f", tax));
        totalText.setText(String.format(java.util.Locale.US, "$%.2f", total));
    }

    private void presentPaymentSheet() {
        // Validate shipping address before proceeding
        if (!validateShippingAddress()) {
            return;
        }
        
        // For a class demo, we'll use a simplified approach
        // In production, you'd call your backend to create a PaymentIntent
        // and use: paymentSheet.presentWithPaymentIntent(clientSecret, configuration);
        
        // For demo purposes, show a dialog to simulate payment
        showMockPaymentDialog();
    }
    
    private boolean validateShippingAddress() {
        String street = streetInput.getText().toString().trim();
        String city = cityInput.getText().toString().trim();
        String state = stateInput.getText().toString().trim();
        String zip = zipInput.getText().toString().trim();
        String country = countryInput.getText().toString().trim();
        
        if (street.isEmpty()) {
            streetInput.setError("Street address is required");
            streetInput.requestFocus();
            return false;
        }
        if (city.isEmpty()) {
            cityInput.setError("City is required");
            cityInput.requestFocus();
            return false;
        }
        if (state.isEmpty()) {
            stateInput.setError("State is required");
            stateInput.requestFocus();
            return false;
        }
        if (zip.isEmpty()) {
            zipInput.setError("Zip code is required");
            zipInput.requestFocus();
            return false;
        }
        if (country.isEmpty()) {
            countryInput.setError("Country is required");
            countryInput.requestFocus();
            return false;
        }
        
        return true;
    }

    private void showMockPaymentDialog() {
        // Create a simple layout for the card input
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        // Use TextInputLayout for proper Material Design styling with outline
        com.google.android.material.textfield.TextInputLayout inputLayout = 
                new com.google.android.material.textfield.TextInputLayout(this);
        inputLayout.setHint("Card Number (e.g. 4242 4242 4242 4242)");
        inputLayout.setBoxBackgroundMode(com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE);
        
        // Set layout parameters for proper width
        android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputLayout.setLayoutParams(layoutParams);
        
        final com.google.android.material.textfield.TextInputEditText cardInput = 
                new com.google.android.material.textfield.TextInputEditText(inputLayout.getContext());
        cardInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        
        inputLayout.addView(cardInput);
        layout.addView(inputLayout);

        // Create and show the dialog with Material theme
        androidx.appcompat.app.AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Pay with Card")
                .setMessage("Total: " + String.format(java.util.Locale.US, "$%.2f", total) + "\n\nEnter test card details below:")
                .setView(layout)
                .setPositiveButton("Pay Now", (dialogInterface, which) -> {
                    String cardNumber = cardInput.getText().toString().replaceAll("\\s+", ""); // Remove spaces
                    
                    // Simulate decline for specific test card ending in 0002
                    if (cardNumber.endsWith("0002")) {
                        Toast.makeText(this, "Card Declined: Your card was declined.", Toast.LENGTH_LONG).show();
                    } else {
                        // All other cards succeed
                        processSuccessfulPayment("card");
                    }
                })
                .setNegativeButton("Google Pay", (dialogInterface, which) -> {
                    processSuccessfulPayment("google_pay");
                })
                .setNeutralButton("Cancel", null)
                .create();
        
        dialog.show();
        
        // Ensure buttons are visible by setting their text color
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(
                    getResources().getColor(R.color.royal_blue, null));
        }
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    getResources().getColor(R.color.royal_blue, null));
        }
        if (dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL) != null) {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(
                    getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private void processSuccessfulPayment(String paymentMethod) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Create shipping address from inputs
        ShippingAddress shippingAddress = new ShippingAddress(
                streetInput.getText().toString().trim(),
                cityInput.getText().toString().trim(),
                stateInput.getText().toString().trim(),
                zipInput.getText().toString().trim(),
                countryInput.getText().toString().trim()
        );
        
        // Create order object
        Order order = new Order(
                orderId,
                userId,
                orderItems,
                subtotal,
                tax,
                total,
                "completed",
                paymentMethod,
                shippingAddress
        );

        // Save order to Firestore
        db.collection("Orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    // Update product stock quantities
                    updateProductStock();
                    
                    // Clear the cart
                    clearCart(userId);
                    
                    // Navigate to success screen
                    Intent intent = new Intent(CheckoutActivity.this, CheckoutSuccessActivity.class);
                    intent.putExtra("ORDER_ID", orderId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save order: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void updateProductStock() {
        // Update stock for each product in the order
        for (OrderItem item : orderItems) {
            // Use FieldValue.increment to safely decrement stock
            // This prevents race conditions if multiple orders are placed simultaneously
            db.collection("Products").document(item.productId)
                    .update("Stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity))
                    .addOnSuccessListener(aVoid -> {
                        // Log successful stock update
                        android.util.Log.d("CheckoutActivity", 
                                "Successfully updated stock for product " + item.productId + 
                                " (decreased by " + item.quantity + ")");
                    })
                    .addOnFailureListener(e -> {
                        // Log error but don't block the order completion
                        android.util.Log.e("CheckoutActivity", 
                                "Failed to update stock for product " + item.productId + ": " + e.getMessage());
                        // Also show a toast to make the error visible
                        Toast.makeText(CheckoutActivity.this, 
                                "Warning: Stock not updated for " + item.productName, 
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void clearCart(String userId) {
        // Delete all items from the cart
        db.collection("ShoppingCarts").document(userId).collection("Items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        doc.getReference().delete();
                    }
                });
    }
}

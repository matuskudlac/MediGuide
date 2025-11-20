package com.team.mediguide.ui.product;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.team.mediguide.CartItem;
import com.team.mediguide.Product;
import com.team.mediguide.R;

import java.util.Date;

public class ProductDetailFragment extends Fragment {

    private FirebaseFirestore db;
    private String productId;
    private Product currentProduct;
    private int selectedQuantity = 1;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        db = FirebaseFirestore.getInstance();
        productId = getArguments().getString("productId");

        // Find views
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        ImageView productImageView = root.findViewById(R.id.product_image);
        TextView productBrandView = root.findViewById(R.id.product_brand);
        TextView productNameView = root.findViewById(R.id.product_name);
        TextView productPriceView = root.findViewById(R.id.product_price);
        TextView productDescriptionView = root.findViewById(R.id.product_description);
        TextView productIngredientsView = root.findViewById(R.id.product_ingredients);
        TextView productRecommendedUsageView = root.findViewById(R.id.product_recommended_usage);
        Button addToCartButton = root.findViewById(R.id.add_to_cart_button);
        LinearLayout quantitySelector = root.findViewById(R.id.quantity_selector);
        Button decrementButton = root.findViewById(R.id.decrement_button);
        Button incrementButton = root.findViewById(R.id.increment_button);
        TextView quantityTextView = root.findViewById(R.id.quantity_textview);

        // Set up the local toolbar
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        toolbar.setTitle("");

        // Set the navigation icon color
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.royal_blue), PorterDuff.Mode.SRC_ATOP);
        }

        // Fetch product details
        db.collection("Products").document(productId).get().addOnSuccessListener(documentSnapshot -> {
            currentProduct = documentSnapshot.toObject(Product.class);
            if (currentProduct != null) {
                productBrandView.setText(currentProduct.brand);
                productNameView.setText(currentProduct.name);
                productPriceView.setText(String.format("$%.2f", currentProduct.price));
                productDescriptionView.setText(currentProduct.description);
                productIngredientsView.setText("Ingredients: " + currentProduct.ingredients);
                productRecommendedUsageView.setText("Usage: " + currentProduct.recommendedUsage);

                Glide.with(getContext()).load(currentProduct.imageUrl).into(productImageView);

                if (currentProduct.stock == 0) {
                    addToCartButton.setEnabled(false);
                    addToCartButton.setText("Out of Stock");
                    quantitySelector.setVisibility(View.GONE);
                } else {
                    setupQuantityControls(decrementButton, incrementButton, quantityTextView);
                }
            }
        });

        addToCartButton.setOnClickListener(v -> addToCart(v));

        return root;
    }

    private void setupQuantityControls(Button decrement, Button increment, TextView quantityView) {
        decrement.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                quantityView.setText(String.valueOf(selectedQuantity));
            }
        });

        increment.setOnClickListener(v -> {
            if (selectedQuantity < currentProduct.stock) {
                selectedQuantity++;
                quantityView.setText(String.valueOf(selectedQuantity));
            } else {
                Toast.makeText(getContext(), "Cannot add more than available stock.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToCart(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        Query query = db.collection("ShoppingCarts").document(userId).collection("Items").whereEqualTo("Product_ID", productId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentReference itemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                int existingQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("Quantity").intValue();
                int newQuantity = existingQuantity + selectedQuantity;

                if (newQuantity > currentProduct.stock) {
                    Toast.makeText(getContext(), "Not enough stock to add quantity.", Toast.LENGTH_SHORT).show();
                    return;
                }
                itemRef.update("Quantity", newQuantity).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cart updated", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                });
            } else {
                CartItem newItem = new CartItem();
                newItem.productId = productId;
                newItem.quantity = selectedQuantity;
                newItem.dateAdded = new Date();
                db.collection("ShoppingCarts").document(userId).collection("Items").add(newItem).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                });
            }
        });
    }
}

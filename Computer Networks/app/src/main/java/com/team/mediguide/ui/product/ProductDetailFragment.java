package com.team.mediguide.ui.product;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team.mediguide.Product;
import com.team.mediguide.R;

public class ProductDetailFragment extends Fragment {

    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        db = FirebaseFirestore.getInstance();

        // Get the product ID from the arguments
        String productId = getArguments().getString("productId");

        // Find views
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        ImageView productImageView = root.findViewById(R.id.product_image);
        TextView productNameView = root.findViewById(R.id.product_name);
        TextView productPriceView = root.findViewById(R.id.product_price);
        TextView productDescriptionView = root.findViewById(R.id.product_description);
        TextView productIngredientsView = root.findViewById(R.id.product_ingredients);
        Button addToCartButton = root.findViewById(R.id.add_to_cart_button);

        // Set up the toolbar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set the up arrow color (safer method)
        if(toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.royal_blue), PorterDuff.Mode.SRC_ATOP);
        }

        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Fetch product details from Firestore
        db.collection("Products").document(productId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Product product = task.getResult().toObject(Product.class);
                if (product != null) {
                    // Populate views with product data
                    productNameView.setText(product.name);
                    productPriceView.setText(String.format("$%.2f", product.price));
                    productDescriptionView.setText(product.description);
                    productIngredientsView.setText("Ingredients: " + product.ingredients);

                    Glide.with(getContext())
                            .load(product.imageUrl)
                            .placeholder(Color.LTGRAY)
                            .into(productImageView);
                }
            } else {
                Toast.makeText(getContext(), "Failed to load product details.", Toast.LENGTH_SHORT).show();
            }
        });

        addToCartButton.setOnClickListener(v -> {
            // TODO: Implement Add to Cart logic
            Toast.makeText(getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
        });

        return root;
    }
}

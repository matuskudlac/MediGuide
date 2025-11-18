package com.team.mediguide.ui.product;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            productId = getArguments().getString("productId");
        }

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(toolbar);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_ATOP);
        }

        CollapsingToolbarLayout collapsingToolbar = root.findViewById(R.id.collapsing_toolbar);
        ImageView productImageView = root.findViewById(R.id.product_image);
        TextView productNameView = root.findViewById(R.id.product_name);
        TextView productBrandView = root.findViewById(R.id.product_brand);
        TextView productPriceView = root.findViewById(R.id.product_price);
        TextView productDescriptionView = root.findViewById(R.id.product_description);
        TextView productIngredientsView = root.findViewById(R.id.product_ingredients);
        TextView productRecommendedUsageView = root.findViewById(R.id.product_recommended_usage);
        FloatingActionButton addToCartFab = root.findViewById(R.id.add_to_cart_fab);

        db.collection("Products").document(productId).get().addOnSuccessListener(documentSnapshot -> {
            currentProduct = documentSnapshot.toObject(Product.class);
            if (currentProduct != null) {
                collapsingToolbar.setTitle(currentProduct.name);
                productNameView.setText(currentProduct.name);
                productBrandView.setText(currentProduct.brand);
                productPriceView.setText(String.format("$%.2f", currentProduct.price));
                productDescriptionView.setText(currentProduct.description);
                productIngredientsView.setText("Ingredients: " + currentProduct.ingredients);
                productRecommendedUsageView.setText("Usage: " + currentProduct.recommendedUsage);

                if (getContext() != null) {
                    Glide.with(getContext()).load(currentProduct.imageUrl).into(productImageView);
                }

                if (currentProduct.stock == 0) {
                    addToCartFab.setEnabled(false);
                }
            }
        });

        addToCartFab.setOnClickListener(v -> addToCart(v));

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.product_detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            if (currentProduct != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this product: " + currentProduct.name + " on MediGuide!");
                startActivity(Intent.createChooser(shareIntent, "Share Product"));
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addToCart(View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        if (currentProduct == null) return;

        String userId = currentUser.getUid();
        Query query = db.collection("ShoppingCarts").document(userId).collection("Items").whereEqualTo("productId", productId);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentReference itemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                long existingQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity");
                long newQuantity = existingQuantity + 1;

                if (newQuantity > currentProduct.stock) {
                    Toast.makeText(getContext(), "Not enough stock to add more.", Toast.LENGTH_SHORT).show();
                    return;
                }
                itemRef.update("quantity", newQuantity).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Cart updated", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                });
            } else {
                if (currentProduct.stock < 1) {
                    Toast.makeText(getContext(), "Out of stock", Toast.LENGTH_SHORT).show();
                    return;
                }
                CartItem newItem = new CartItem();
                newItem.productId = productId;
                newItem.quantity = 1;
                newItem.dateAdded = new Date();
                db.collection("ShoppingCarts").document(userId).collection("Items").add(newItem).addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
                });
            }
        });
    }
}

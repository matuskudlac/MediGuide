package com.team.mediguide;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.name);
        holder.productPrice.setText(String.format("$%.2f", product.price));

        if (product.stock > 10) {
            holder.productStock.setText("In Stock");
            holder.productStock.setTextColor(Color.rgb(0, 150, 0));
        } else if (product.stock > 0) {
            holder.productStock.setText(product.stock + " left in stock");
            holder.productStock.setTextColor(Color.rgb(255, 165, 0));
        } else {
            holder.productStock.setText("Out of Stock");
            holder.productStock.setTextColor(Color.RED);
        }

        // Set initial visibility before loading
        holder.productImage.setVisibility(View.VISIBLE);
        holder.imageErrorText.setVisibility(View.GONE);

        Glide.with(holder.itemView.getContext())
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.productImage.setVisibility(View.GONE);
                        holder.imageErrorText.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.productImage.setVisibility(View.VISIBLE);
                        holder.imageErrorText.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.productImage);

        // Set listener for the entire item to navigate to detail page
        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.id);
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_productDetailFragment, bundle);
        });

        // Handle the new "Add to Cart" button
        if (product.stock > 0) {
            holder.addToCartButton.setVisibility(View.VISIBLE);
            holder.addToCartButton.setOnClickListener(v -> {
                addToCart(product, v);
            });
        } else {
            holder.addToCartButton.setVisibility(View.GONE);
        }
    }

    private void addToCart(Product product, View view) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("ShoppingCarts").document(userId).collection("Items")
                .whereEqualTo("productId", product.id).limit(1).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Item exists, update quantity by 1
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        long newQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity") + 1;
                        db.collection("ShoppingCarts").document(userId).collection("Items")
                                .document(docId).update("quantity", newQuantity);
                        Toast.makeText(view.getContext(), "Item quantity updated", Toast.LENGTH_SHORT).show();
                    } else {
                        // Item does not exist, add new with quantity 1
                        CartItem newItem = new CartItem();
                        newItem.productId = product.id;
                        newItem.quantity = 1;
                        newItem.dateAdded = new Date();
                        db.collection("ShoppingCarts").document(userId).collection("Items").add(newItem);
                        Toast.makeText(view.getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productStock;
        TextView imageErrorText;
        Button addToCartButton; // Add button reference

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
            imageErrorText = itemView.findViewById(R.id.imageErrorText);
            addToCartButton = itemView.findViewById(R.id.addToCartButton); // Initialize button
        }
    }
}
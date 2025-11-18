package com.team.mediguide;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private List<Product> productList;
    private List<Product> productListFull;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setProductListFull(List<Product> productListFull) {
        this.productListFull = productListFull;
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
            holder.productStock.setTextColor(Color.GREEN);
        } else if (product.stock > 0) {
            holder.productStock.setText(product.stock + " left in stock");
            holder.productStock.setTextColor(Color.rgb(255, 165, 0)); // Orange
        } else {
            holder.productStock.setText("Out of Stock");
            holder.productStock.setTextColor(Color.RED);
        }

        Glide.with(holder.itemView.getContext())
                .load(product.imageUrl)
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.id);
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_productDetailFragment, bundle);
        });

        holder.addToCartButton.setOnClickListener(v -> addToCart(v, product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Product item : productListFull) {
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    private void addToCart(View view, Product product) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = currentUser.getUid();
        Query query = db.collection("ShoppingCarts").document(userId).collection("Items").whereEqualTo("productId", product.id);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                DocumentReference itemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                long existingQuantity = queryDocumentSnapshots.getDocuments().get(0).getLong("quantity");
                itemRef.update("quantity", existingQuantity + 1).addOnSuccessListener(aVoid -> {
                    Toast.makeText(view.getContext(), "Cart updated", Toast.LENGTH_SHORT).show();
                });
            } else {
                CartItem newItem = new CartItem();
                newItem.productId = product.id;
                newItem.quantity = 1;
                newItem.dateAdded = new Date();
                db.collection("ShoppingCarts").document(userId).collection("Items").add(newItem).addOnSuccessListener(aVoid -> {
                    Toast.makeText(view.getContext(), "Added to cart", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productStock;
        Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
            addToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}

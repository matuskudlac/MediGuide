package com.team.mediguide;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

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
            holder.productStock.setTextColor(Color.rgb(255, 165, 0)); // Orange
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
                        // If the image fails to load, hide the image view and show the error text
                        holder.productImage.setVisibility(View.GONE);
                        holder.imageErrorText.setVisibility(View.VISIBLE);
                        return false; // Return false so Glide can handle any error placeholder
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // If the image loads, ensure the error text is hidden
                        holder.productImage.setVisibility(View.VISIBLE);
                        holder.imageErrorText.setVisibility(View.GONE);
                        return false; // Return false to let Glide handle displaying the image
                    }
                })
                .into(holder.productImage);

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.id);
            Navigation.findNavController(v).navigate(R.id.action_navigation_home_to_productDetailFragment, bundle);
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
        TextView imageErrorText; // Add a reference to the error TextView

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
            imageErrorText = itemView.findViewById(R.id.imageErrorText); // Initialize it
        }
    }
}

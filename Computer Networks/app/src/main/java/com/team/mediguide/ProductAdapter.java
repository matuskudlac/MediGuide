package com.team.mediguide;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Locale;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;

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
        Context context = holder.itemView.getContext();

        holder.productName.setText(product.name);
        holder.productPrice.setText(String.format(Locale.US, "$%.2f", product.price));

        if (product.stock > 10) {
            holder.productStock.setText(R.string.in_stock);
            holder.productStock.setTextColor(Color.rgb(0, 150, 0));
        } else if (product.stock > 0) {
            holder.productStock.setText(context.getString(R.string.stock_left_in_stock, product.stock));
            holder.productStock.setTextColor(Color.rgb(255, 165, 0)); // Orange
        } else {
            holder.productStock.setText(R.string.out_of_stock);
            holder.productStock.setTextColor(Color.RED);
        }

        Glide.with(context)
                .load(product.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(new TextDrawable(context.getString(R.string.image_not_found)))
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

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productStock;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productStock = itemView.findViewById(R.id.productStock);
        }
    }
}

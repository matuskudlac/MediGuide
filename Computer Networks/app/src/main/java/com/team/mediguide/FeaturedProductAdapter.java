package com.team.mediguide;

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

public class FeaturedProductAdapter extends RecyclerView.Adapter<FeaturedProductAdapter.FeaturedProductViewHolder> {

    private List<Product> productList;

    public FeaturedProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public FeaturedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.featured_product_item, parent, false);
        return new FeaturedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.name);

        Glide.with(holder.itemView.getContext())
                .load(product.imageUrl)
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

    static class FeaturedProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;

        public FeaturedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.featured_product_image);
            productName = itemView.findViewById(R.id.featured_product_name);
        }
    }
}

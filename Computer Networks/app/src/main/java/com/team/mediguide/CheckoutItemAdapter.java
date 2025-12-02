package com.team.mediguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.CheckoutItemViewHolder> {

    private List<OrderItem> orderItems;

    public CheckoutItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public CheckoutItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.checkout_item, parent, false);
        return new CheckoutItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        
        holder.productName.setText(item.productName);
        holder.quantityPrice.setText(String.format("%d × $%.2f", item.quantity, item.price));
        holder.lineTotal.setText(String.format("$%.2f", item.getLineTotal()));
        
        Glide.with(holder.itemView.getContext())
                .load(item.imageUrl)
                .into(holder.productImage);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class CheckoutItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView quantityPrice;
        TextView lineTotal;

        public CheckoutItemViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.checkout_product_image);
            productName = itemView.findViewById(R.id.checkout_product_name);
            quantityPrice = itemView.findViewById(R.id.checkout_quantity_price);
            lineTotal = itemView.findViewById(R.id.checkout_line_total);
        }
    }
}

package com.team.mediguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orders;

    public OrderAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView orderDate;
        private final TextView orderId;
        private final TextView orderTotal;
        private final TextView orderStatus;
        private final TextView orderItemCount;
        private final TextView shippingAddress;
        private final RecyclerView orderItemsRecycler;
        private final LinearLayout detailsSection;
        private final Button toggleButton;

        private boolean isExpanded = false;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDate = itemView.findViewById(R.id.order_date);
            orderId = itemView.findViewById(R.id.order_id);
            orderTotal = itemView.findViewById(R.id.order_total);
            orderStatus = itemView.findViewById(R.id.order_status);
            orderItemCount = itemView.findViewById(R.id.order_item_count);
            shippingAddress = itemView.findViewById(R.id.shipping_address);
            orderItemsRecycler = itemView.findViewById(R.id.order_items_recycler);
            detailsSection = itemView.findViewById(R.id.order_details_section);
            toggleButton = itemView.findViewById(R.id.toggle_details_button);
        }

        public void bind(Order order) {
            // Format date and time
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.US);
            if (order.orderDate != null) {
                orderDate.setText(dateFormat.format(order.orderDate));
            } else {
                orderDate.setText("N/A");
            }

            // Set order ID (truncated for display)
            String shortId = order.orderId.length() > 8 ? order.orderId.substring(0, 8) : order.orderId;
            orderId.setText("Order #" + shortId);

            // Set total
            orderTotal.setText(String.format(Locale.US, "$%.2f", order.total));

            // Set status
            String status = order.paymentStatus != null ? order.paymentStatus : "unknown";
            orderStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));

            // Set item count
            int itemCount = order.items != null ? order.items.size() : 0;
            orderItemCount.setText("• " + itemCount + " item" + (itemCount != 1 ? "s" : ""));

            // Set shipping address
            if (order.shippingAddress != null) {
                shippingAddress.setText(order.shippingAddress.getFullAddress());
            } else {
                shippingAddress.setText("No shipping address");
            }

            // Setup items RecyclerView
            if (order.items != null && !order.items.isEmpty()) {
                orderItemsRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                orderItemsRecycler.setAdapter(new CheckoutItemAdapter(order.items));
            }

            // Toggle details
            toggleButton.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                detailsSection.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                toggleButton.setText(isExpanded ? "Hide Details" : "View Details");
            });
        }
    }
}

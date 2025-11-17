package com.team.mediguide;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CartAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);

        holder.cartQuantityTextView.setText(String.valueOf(cartItem.quantity));

        // Set button colors
        int blueColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.royal_blue);
        holder.decrementButton.setTextColor(blueColor);
        holder.incrementButton.setTextColor(blueColor);
        ImageViewCompat.setImageTintList(holder.removeButton, ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.royal_blue));

        // Fetch product details
        db.collection("Products").document(cartItem.productId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Product product = documentSnapshot.toObject(Product.class);
                holder.cartProductName.setText(product.name);
                holder.cartProductPrice.setText(String.format("$%.2f", product.price));
                Glide.with(holder.itemView.getContext()).load(product.imageUrl).into(holder.cartProductImage);

                setupCartControls(holder, cartItem, product);
            }
        });
    }

    private void setupCartControls(CartViewHolder holder, CartItem cartItem, Product product) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference itemRef = db.collection("ShoppingCarts").document(userId).collection("Items").document(cartItem.id);

        holder.removeButton.setOnClickListener(v -> itemRef.delete());

        holder.decrementButton.setOnClickListener(v -> {
            if (cartItem.quantity > 1) {
                itemRef.update("Quantity", cartItem.quantity - 1);
            } else {
                itemRef.delete();
            }
        });

        holder.incrementButton.setOnClickListener(v -> {
            if (cartItem.quantity < product.stock) {
                itemRef.update("Quantity", cartItem.quantity + 1);
            } else {
                Toast.makeText(holder.itemView.getContext(), "No more stock available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartProductImage;
        TextView cartProductName;
        TextView cartProductPrice;
        TextView cartQuantityTextView;
        Button decrementButton, incrementButton;
        ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartProductImage = itemView.findViewById(R.id.cart_product_image);
            cartProductName = itemView.findViewById(R.id.cart_product_name);
            cartProductPrice = itemView.findViewById(R.id.cart_product_price);
            cartQuantityTextView = itemView.findViewById(R.id.cart_quantity_textview);
            decrementButton = itemView.findViewById(R.id.cart_decrement_button);
            incrementButton = itemView.findViewById(R.id.cart_increment_button);
            removeButton = itemView.findViewById(R.id.cart_remove_button);
        }
    }
}

package com.team.mediguide.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.team.mediguide.CartAdapter;
import com.team.mediguide.CartItem;
import com.team.mediguide.R;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        cartRecyclerView = root.findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItems);
        cartRecyclerView.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();

        listenForCartUpdates();

        Button checkoutButton = root.findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(v -> {
            // TODO: Implement checkout logic
            Toast.makeText(getContext(), "Checkout not yet implemented.", Toast.LENGTH_SHORT).show();
        });

        return root;
    }

    private void listenForCartUpdates() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("ShoppingCarts").document(userId).collection("Items")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            // Handle error
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            CartItem item = dc.getDocument().toObject(CartItem.class);
                            item.id = dc.getDocument().getId();

                            switch (dc.getType()) {
                                case ADDED:
                                    cartItems.add(item);
                                    break;
                                case MODIFIED:
                                    // Find item and update it
                                    for (int i = 0; i < cartItems.size(); i++) {
                                        if (cartItems.get(i).id.equals(item.id)) {
                                            cartItems.set(i, item);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    cartItems.removeIf(cartItem -> cartItem.id.equals(item.id));
                                    break;
                            }
                        }
                        cartAdapter.notifyDataSetChanged();
                    });
        }
    }
}

package com.team.mediguide;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView ordersRecycler;
    private LinearLayout emptyState;
    private OrderAdapter adapter;
    private List<Order> orders;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        orders = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.order_history_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        ordersRecycler = findViewById(R.id.orders_recycler);
        emptyState = findViewById(R.id.empty_state);

        // Setup RecyclerView
        ordersRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orders);
        ordersRecycler.setAdapter(adapter);

        // Load orders
        loadOrders();
    }

    private void loadOrders() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            emptyState.setVisibility(View.VISIBLE);
            ordersRecycler.setVisibility(View.GONE);
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        
        // Check if user is admin
        boolean isAdmin = userEmail != null && userEmail.equals("admin@mediguide.com");

        // Build query based on admin status
        Query query;
        if (isAdmin) {
            // Admin sees all orders from all users
            query = db.collection("Orders");
        } else {
            // Regular users see only their own orders
            query = db.collection("Orders").whereEqualTo("User_ID", userId);
        }
        
        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orders.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        orders.add(order);
                    }
                    
                    // Sort orders by date (newest first) in memory
                    orders.sort((o1, o2) -> {
                        if (o1.orderDate == null && o2.orderDate == null) return 0;
                        if (o1.orderDate == null) return 1;
                        if (o2.orderDate == null) return -1;
                        return o2.orderDate.compareTo(o1.orderDate);
                    });
                    
                    if (orders.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        ordersRecycler.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        ordersRecycler.setVisibility(View.VISIBLE);
                    }
                    
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Show empty state on error and log the error
                    android.widget.Toast.makeText(this, "Error loading orders: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    emptyState.setVisibility(View.VISIBLE);
                    ordersRecycler.setVisibility(View.GONE);
                });
    }
}

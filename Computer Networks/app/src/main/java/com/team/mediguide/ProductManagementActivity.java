package com.team.mediguide;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView productsRecycler;
    private ProductManagementAdapter adapter;
    private List<Product> products;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);

        db = FirebaseFirestore.getInstance();
        products = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.product_management_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize RecyclerView
        productsRecycler = findViewById(R.id.products_recycler);
        productsRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductManagementAdapter(products, this);
        productsRecycler.setAdapter(adapter);

        // FAB for adding products
        FloatingActionButton fab = findViewById(R.id.fab_add_product);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditProductActivity.class);
            startActivity(intent);
        });

        // Load products
        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Refresh when returning from add/edit
    }

    public void loadProducts() {
        db.collection("Products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    products.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        product.setId(document.getId());
                        products.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Error loading products: " + e.getMessage(),
                            android.widget.Toast.LENGTH_SHORT).show();
                });
    }
}

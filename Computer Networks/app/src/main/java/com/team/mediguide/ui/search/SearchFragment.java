package com.team.mediguide.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.team.mediguide.Product;
import com.team.mediguide.ProductAdapter;
import com.team.mediguide.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private RecyclerView searchRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> allProducts;
    private List<Product> filteredProducts;
    private FirebaseFirestore db;
    private SearchView searchView;
    private TextView emptyStateText;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchView = root.findViewById(R.id.searchView);
        searchRecyclerView = root.findViewById(R.id.searchRecyclerView);
        emptyStateText = root.findViewById(R.id.emptyStateText);

        // Setup RecyclerView
        searchRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        allProducts = new ArrayList<>();
        filteredProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(filteredProducts);
        searchRecyclerView.setAdapter(productAdapter);

        // Setup SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });

        // Ensure SearchView is always expanded and ready for input
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        searchView.clearFocus(); // Clear focus so keyboard doesn't auto-show, but bar is still clickable

        // Fetch all products
        fetchProducts();

        return root;
    }

    private void fetchProducts() {
        db.collection("Products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Task successful");
                        allProducts.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Product product = document.toObject(Product.class);
                                product.id = document.getId();
                                allProducts.add(product);
                                Log.d(TAG, "Product added: " + product.name);
                            } catch (Exception e) {
                                Log.e(TAG, "Error converting document to Product", e);
                            }
                        }
                        // Initially show all products
                        filteredProducts.clear();
                        filteredProducts.addAll(allProducts);
                        productAdapter.notifyDataSetChanged();
                        updateEmptyState();
                        Log.d(TAG, "Product list size: " + allProducts.size());
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    private void filterProducts(String query) {
        filteredProducts.clear();

        if (query == null || query.trim().isEmpty()) {
            // If search is empty, show all products
            filteredProducts.addAll(allProducts);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            
            for (Product product : allProducts) {
                // Search in product name
                if (product.name != null && product.name.toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                    continue;
                }
                
                // Search in brand
                if (product.brand != null && product.brand.toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                    continue;
                }
                
                // Search in category
                if (product.categoryId != null && product.categoryId.toLowerCase().contains(lowerCaseQuery)) {
                    filteredProducts.add(product);
                    continue;
                }
                
                // Search in keywords
                if (product.keywords != null) {
                    for (String keyword : product.keywords) {
                        if (keyword != null && keyword.toLowerCase().contains(lowerCaseQuery)) {
                            filteredProducts.add(product);
                            break;
                        }
                    }
                }
            }
        }

        productAdapter.notifyDataSetChanged();
        updateEmptyState();
        Log.d(TAG, "Filtered products: " + filteredProducts.size() + " for query: " + query);
    }

    private void updateEmptyState() {
        if (filteredProducts.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            searchRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            searchRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

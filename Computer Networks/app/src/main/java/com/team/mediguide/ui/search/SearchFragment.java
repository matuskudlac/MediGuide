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
                // Show/hide close button based on whether there's text
                updateCloseButtonVisibility(newText);
                return true;
            }
        });
        
        // Add focus change listener to update close button visibility
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && searchView.getQuery().toString().isEmpty()) {
                updateCloseButtonVisibility("");
            }
        });
        
        // Prevent SearchView from collapsing when close button is clicked
        searchView.setOnCloseListener(() -> {
            // Return false to allow the default close behavior (clearing text)
            // but keep the SearchView expanded
            searchView.setIconified(false);
            return false;
        });

        // Ensure SearchView is always expanded and ready for input
        searchView.setIconified(false);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        searchView.clearFocus(); // Clear focus so keyboard doesn't auto-show, but bar is still clickable
        
        // Initially hide the close button after view is fully created
        // Use multiple delayed posts to ensure it catches the button
        searchView.post(() -> updateCloseButtonVisibility(""));
        searchView.postDelayed(() -> updateCloseButtonVisibility(""), 100);
        searchView.postDelayed(() -> updateCloseButtonVisibility(""), 300);

        // Fetch all products
        fetchProducts();

        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh product data whenever the fragment becomes visible
        // This ensures stock quantities are up-to-date after orders are placed
        fetchProducts();
    }
    
    private void updateCloseButtonVisibility(String query) {
        // Find the close button view and hide/show it based on query text
        boolean shouldShow = query != null && !query.isEmpty();
        
        try {
            // Method 1: Try standard Android ID
            int closeButtonId = searchView.getContext().getResources()
                    .getIdentifier("search_close_btn", "id", "android");
            
            if (closeButtonId != 0) {
                View closeButton = searchView.findViewById(closeButtonId);
                if (closeButton != null) {
                    closeButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                    return;
                }
            }
            
            // Method 2: Try androidx package
            closeButtonId = searchView.getContext().getResources()
                    .getIdentifier("search_close_btn", "id", searchView.getContext().getPackageName());
            
            if (closeButtonId != 0) {
                View closeButton = searchView.findViewById(closeButtonId);
                if (closeButton != null) {
                    closeButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
                    return;
                }
            }
            
            // Method 3: Search through view hierarchy
            View closeButton = findViewByClassName(searchView, "ImageView");
            if (closeButton != null && closeButton.getContentDescription() != null && 
                closeButton.getContentDescription().toString().toLowerCase().contains("clear")) {
                closeButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating close button visibility", e);
        }
    }
    
    private View findViewByClassName(ViewGroup parent, String className) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getClass().getSimpleName().equals(className)) {
                // Check if this ImageView is likely the close button
                // Close buttons typically have a content description
                if (child.getContentDescription() != null) {
                    String desc = child.getContentDescription().toString().toLowerCase();
                    if (desc.contains("clear") || desc.contains("close")) {
                        return child;
                    }
                }
            }
            if (child instanceof ViewGroup) {
                View result = findViewByClassName((ViewGroup) child, className);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
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

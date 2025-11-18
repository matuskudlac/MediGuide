package com.team.mediguide.ui.home;

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
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.team.mediguide.FeaturedProductAdapter;
import com.team.mediguide.Product;
import com.team.mediguide.ProductAdapter;
import com.team.mediguide.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView productsRecyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;
    private ViewPager2 featuredProductsViewPager;
    private FeaturedProductAdapter featuredProductAdapter;
    private List<Product> featuredProductList;
    private FirebaseFirestore db;
    private SearchView searchView;
    private TextView welcomeMessage;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        welcomeMessage = root.findViewById(R.id.welcome_message);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            welcomeMessage.setText("Welcome, " + user.getDisplayName() + "!");
        }

        featuredProductsViewPager = root.findViewById(R.id.featured_products_viewpager);
        featuredProductList = new ArrayList<>();
        featuredProductAdapter = new FeaturedProductAdapter(featuredProductList);
        featuredProductsViewPager.setAdapter(featuredProductAdapter);

        productsRecyclerView = root.findViewById(R.id.productsRecyclerView);
        productsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        productsRecyclerView.setAdapter(productAdapter);

        searchView = root.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                productAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText);
                return false;
            }
        });

        fetchFeaturedProducts();
        fetchProducts();

        return root;
    }

    private void fetchFeaturedProducts() {
        db.collection("Products").whereEqualTo("isFeatured", true).limit(5)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        featuredProductList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.id = document.getId();
                            featuredProductList.add(product);
                        }
                        featuredProductAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting featured documents.", task.getException());
                    }
                });
    }

    private void fetchProducts() {
        db.collection("Products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Product product = document.toObject(Product.class);
                            product.id = document.getId();
                            productList.add(product);
                        }
                        productAdapter.setProductListFull(new ArrayList<>(productList));
                        productAdapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}

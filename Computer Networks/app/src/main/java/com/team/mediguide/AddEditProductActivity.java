package com.team.mediguide;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddEditProductActivity extends AppCompatActivity {

    private TextInputEditText nameInput, descriptionInput, priceInput, imageInput;
    private AutoCompleteTextView categoryInput;
    private Button saveButton, cancelButton;
    private FirebaseFirestore db;
    private String productId = null; // null for add, set for edit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        db = FirebaseFirestore.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.add_edit_product_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        nameInput = findViewById(R.id.input_product_name);
        descriptionInput = findViewById(R.id.input_product_description);
        priceInput = findViewById(R.id.input_product_price);
        imageInput = findViewById(R.id.input_product_image);
        categoryInput = findViewById(R.id.input_product_category);
        saveButton = findViewById(R.id.btn_save);
        cancelButton = findViewById(R.id.btn_cancel);
        
        // Setup category dropdown with predefined categories
        String[] categories = new String[]{
            "Vitamins & Supplements",
            "Pain Relief",
            "First Aid",
            "Personal Care",
            "Digestive Health",
            "Cold & Flu",
            "Skin Care",
            "Eye Care",
            "Oral Care",
            "Other"
        };
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryInput.setAdapter(categoryAdapter);

        // Check if editing existing product
        if (getIntent().hasExtra("PRODUCT_ID")) {
            productId = getIntent().getStringExtra("PRODUCT_ID");
            toolbar.setTitle("Edit Product");
            loadProductData();
        } else {
            toolbar.setTitle("Add Product");
        }

        // Save button
        saveButton.setOnClickListener(v -> saveProduct());

        // Cancel button
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadProductData() {
        nameInput.setText(getIntent().getStringExtra("PRODUCT_NAME"));
        descriptionInput.setText(getIntent().getStringExtra("PRODUCT_DESCRIPTION"));
        priceInput.setText(String.valueOf(getIntent().getDoubleExtra("PRODUCT_PRICE", 0.0)));
        imageInput.setText(getIntent().getStringExtra("PRODUCT_IMAGE"));
        categoryInput.setText(getIntent().getStringExtra("PRODUCT_CATEGORY"));
    }

    private void saveProduct() {
        // Validate inputs
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";
        String description = descriptionInput.getText() != null ? descriptionInput.getText().toString().trim() : "";
        String priceStr = priceInput.getText() != null ? priceInput.getText().toString().trim() : "";
        String image = imageInput.getText() != null ? imageInput.getText().toString().trim() : "";
        String category = categoryInput.getText() != null ? categoryInput.getText().toString().trim() : "";

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            return;
        }
        if (description.isEmpty()) {
            descriptionInput.setError("Description is required");
            return;
        }
        if (priceStr.isEmpty()) {
            priceInput.setError("Price is required");
            return;
        }
        if (image.isEmpty()) {
            imageInput.setError("Image URL is required");
            return;
        }
        if (category.isEmpty()) {
            categoryInput.setError("Category is required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            priceInput.setError("Invalid price");
            return;
        }

        // Create product data
        Map<String, Object> productData = new HashMap<>();
        productData.put("Name", name);
        productData.put("Description", description);
        productData.put("Price", price);
        productData.put("Image_URL", image);
        productData.put("Category_ID", category);

        // Save to Firestore
        if (productId != null) {
            // Update existing product
            db.collection("Products")
                    .document(productId)
                    .update(productData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error updating product: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add new product
            db.collection("Products")
                    .add(productData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error adding product: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

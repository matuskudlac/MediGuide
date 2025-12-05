package com.team.mediguide;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

public class ProductManagementAdapter extends RecyclerView.Adapter<ProductManagementAdapter.ProductViewHolder> {

    private final List<Product> products;
    private final ProductManagementActivity activity;

    public ProductManagementAdapter(List<Product> products, ProductManagementActivity activity) {
        this.products = products;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productCategory;
        private final TextView productPrice;
        private final Button editButton;
        private final Button deleteButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.admin_product_image);
            productName = itemView.findViewById(R.id.admin_product_name);
            productCategory = itemView.findViewById(R.id.admin_product_category);
            productPrice = itemView.findViewById(R.id.admin_product_price);
            editButton = itemView.findViewById(R.id.btn_edit_product);
            deleteButton = itemView.findViewById(R.id.btn_delete_product);
        }

        public void bind(Product product) {
            productName.setText(product.getName());
            productCategory.setText(product.getCategory());
            productPrice.setText(String.format(Locale.US, "$%.2f", product.getPrice()));

            // Load image
            Glide.with(itemView.getContext())
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(productImage);

            // Edit button
            editButton.setOnClickListener(v -> {
                Intent intent = new Intent(activity, AddEditProductActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                intent.putExtra("PRODUCT_NAME", product.getName());
                intent.putExtra("PRODUCT_DESCRIPTION", product.getDescription());
                intent.putExtra("PRODUCT_PRICE", product.getPrice());
                intent.putExtra("PRODUCT_IMAGE", product.getImage());
                intent.putExtra("PRODUCT_CATEGORY", product.getCategory());
                activity.startActivity(intent);
            });

            // Delete button
            deleteButton.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(activity, androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to delete \"" + product.getName() + "\"?")
                        .setPositiveButton("Delete", (dialogInterface, which) -> deleteProduct(product))
                        .setNegativeButton("Cancel", null)
                        .create();
                
                dialog.show();
                
                // Set button colors to match the app theme
                if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                            activity.getResources().getColor(R.color.royal_blue, null));
                }
                if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                            activity.getResources().getColor(R.color.royal_blue, null));
                }
            });
        }

        private void deleteProduct(Product product) {
            FirebaseFirestore.getInstance()
                    .collection("Products")
                    .document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(activity, "Product deleted", Toast.LENGTH_SHORT).show();
                        activity.loadProducts(); // Refresh list
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(activity, "Error deleting product: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

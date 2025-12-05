package com.team.mediguide.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.team.mediguide.AdminHelper;
import com.team.mediguide.MainActivity;
import com.team.mediguide.OrderHistoryActivity;
import com.team.mediguide.ProductManagementActivity;
import com.team.mediguide.R;

public class ProfileFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        Button manageProductsButton = root.findViewById(R.id.manageProductsButton);
        Button viewOrdersButton = root.findViewById(R.id.viewOrdersButton);
        Button changePasswordButton = root.findViewById(R.id.changePasswordButton);
        Button signOutButton = root.findViewById(R.id.signOutButton);

        // Check if user is admin and show/hide Manage Products button
        if (AdminHelper.isAdmin()) {
            manageProductsButton.setVisibility(View.VISIBLE);
            manageProductsButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ProductManagementActivity.class);
                startActivity(intent);
            });
        } else {
            manageProductsButton.setVisibility(View.GONE);
        }

        viewOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());

        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            AdminHelper.clearCache(); // Clear admin cache on logout
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return root;
    }

    private void showChangePasswordDialog() {
        if (getActivity() == null) return;

        // Create input fields
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText currentPasswordInput = new EditText(getActivity());
        currentPasswordInput.setHint("Current Password");
        currentPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(currentPasswordInput);

        final EditText newPasswordInput = new EditText(getActivity());
        newPasswordInput.setHint("New Password (min 6 characters)");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(getActivity());
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), androidx.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("Change", (dialogInterface, which) -> {
                    String currentPassword = currentPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();
                    
                    changePassword(currentPassword, newPassword, confirmPassword);
                })
                .setNegativeButton("Cancel", null)
                .show();
        
        // Set button colors to royal blue to make them visible
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    getResources().getColor(R.color.royal_blue, null));
        }
        if (dialog.getButton(AlertDialog.BUTTON_NEGATIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    getResources().getColor(R.color.royal_blue, null));
        }
    }

    private void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        // Validate inputs
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(getActivity(), "Please enter current password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(getActivity(), "New password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Re-authenticate user with current password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        user.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Password changed successfully!", 
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMessage = "Failed to change password";
                                        if (updateTask.getException() != null) {
                                            errorMessage = updateTask.getException().getMessage();
                                        }
                                        Toast.makeText(getActivity(), errorMessage, 
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(getActivity(), "Current password is incorrect", 
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

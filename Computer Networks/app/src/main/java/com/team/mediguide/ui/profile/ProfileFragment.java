package com.team.mediguide.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
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

        signOutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            AdminHelper.clearCache(); // Clear admin cache on logout
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return root;
    }
}

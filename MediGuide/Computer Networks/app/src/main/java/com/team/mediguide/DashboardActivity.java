package com.team.mediguide;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // This sets up the basic navigation, saving the back stack of each tab
        NavigationUI.setupWithNavController(navView, navController);

        // This adds the custom logic for re-selecting a tab
        navView.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                // When the home item is re-selected, pop the back stack to the home fragment
                navController.popBackStack(R.id.navigation_home, false);
            }
            // This can be extended to reset other tabs if they have nested screens
        });
    }

}

package com.team.mediguide;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.EdgeToEdge;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Apply padding for system bars (edge-to-edge support)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get username from Intent
        String username = getIntent().getStringExtra("USERNAME");

        // Display welcome message
        TextView welcomeText = findViewById(R.id.welcomeText);
        if (username != null && !username.isEmpty()) {
            welcomeText.setText("Welcome, " + username + "!");
        } else {
            welcomeText.setText("Welcome to MediGuide!");
        }
    }
}
package com.team.mediguide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String user = usernameEditText.getText().toString().trim();
            String pass = passwordEditText.getText().toString().trim();

            // Hardcoded credentials for now
            if (user.equals("admin") && pass.equals("1234")) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                // Open DashboardActivity and pass the username
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                intent.putExtra("USERNAME", user);
                startActivity(intent);

                // Optional: close login screen so user can't go back
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
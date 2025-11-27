package com.team.mediguide;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput, confirmPasswordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        confirmPasswordInput = findViewById(R.id.confirm_password);
        Button signUpButton = findViewById(R.id.signUpButton);
        TextView loginLink = findViewById(R.id.loginLink);

        // Sign up button click handler
        signUpButton.setOnClickListener(v -> createAccount());

        // Login link click handler
        loginLink.setOnClickListener(v -> {
            finish(); // Go back to login screen
        });
    }

    private void createAccount() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString().trim() : "";

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInput.setError("Please confirm your password");
            confirmPasswordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Create account with Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        Toast.makeText(SignUpActivity.this, "Account created successfully!", 
                                Toast.LENGTH_SHORT).show();
                        
                        FirebaseUser user = mAuth.getCurrentUser();
                        
                        // Navigate to Dashboard
                        Intent intent = new Intent(SignUpActivity.this, DashboardActivity.class);
                        if (user != null) {
                            intent.putExtra("USERNAME", user.getEmail());
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // Sign up failed
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("email address is already in use")) {
                                    errorMessage = "Email already registered. Please login.";
                                } else if (exceptionMessage.contains("password")) {
                                    errorMessage = "Password is too weak";
                                } else if (exceptionMessage.contains("email")) {
                                    errorMessage = "Invalid email format";
                                } else {
                                    errorMessage = exceptionMessage;
                                }
                            }
                        }
                        Toast.makeText(SignUpActivity.this, errorMessage, 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}

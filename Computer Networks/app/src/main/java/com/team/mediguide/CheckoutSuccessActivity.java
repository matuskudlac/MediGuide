package com.team.mediguide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CheckoutSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_success);

        String orderId = getIntent().getStringExtra("ORDER_ID");
        
        TextView orderIdValue = findViewById(R.id.order_id_value);
        orderIdValue.setText(orderId);

        Button continueShoppingButton = findViewById(R.id.continue_shopping_button);
        continueShoppingButton.setOnClickListener(v -> {
            // Navigate back to dashboard/home
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

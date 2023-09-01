package com.example.AndroidPaymentApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        ImageView createPaymentButton = findViewById(R.id.create_payment);
        createPaymentButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
            startActivity(intent);
        });
        ImageView changeMerchantKeyButton = findViewById(R.id.merchant_key);
        changeMerchantKeyButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartScreenActivity.this, MerchantKeyScreenActivity.class);
            startActivity(intent);
        });
    }
}
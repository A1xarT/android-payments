package com.example.AndroidPaymentApp;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MerchantKeyScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_key_screen);

        AppCompatButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            onBackPressed();
        });
        ImageView backButton = findViewById(R.id.back_button_merchant);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });

    }
}
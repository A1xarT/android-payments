package com.example.AndroidPaymentApp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        startUpdatesCheckService();
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

    private void startUpdatesCheckService() {
        AppUpdater appUpdater = new AppUpdater(this)
                .setUpdateFrom(UpdateFrom.JSON)
                .setUpdateJSON("https://raw.githubusercontent.com/A1xarT/android-payments/main/update-changelog.json")
                .setButtonDoNotShowAgain(null)
                .setCancelable(false);
        appUpdater.start();
    }
}
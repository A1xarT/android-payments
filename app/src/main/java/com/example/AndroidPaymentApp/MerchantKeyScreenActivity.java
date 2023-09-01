package com.example.AndroidPaymentApp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.AndroidPaymentApp.models.KeysBundle;
import com.example.AndroidPaymentApp.models.KeysEntity;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MerchantKeyScreenActivity extends AppCompatActivity {
    public static String currentPublicKey = BuildConfig.REVOLUT_MERCHANT_API_KEY;
    public static KeysBundle keysBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merchant_key_screen);

        initKeysBundle();
        refreshKeys();

        setKeysDropdown();
        setDeleteKeyButton();
        setContinueButton();

        AppCompatButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> {
            resetInputFields();
        });
        ImageView backButton = findViewById(R.id.back_button_merchant);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });
    }
    private void resetInputFields(){
        EditText publicKeyField = findViewById(R.id.new_key_public);
        EditText secretKeyField = findViewById(R.id.new_key_secret);
        publicKeyField.setText("");
        secretKeyField.setText("");
    }
    private void setContinueButton() {
        AppCompatButton continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(v -> {
            EditText publicKeyField = findViewById(R.id.new_key_public);
            EditText secretKeyField = findViewById(R.id.new_key_secret);
            String newPublicKey = publicKeyField.getText().toString();
            String newSecretKey = secretKeyField.getText().toString();
            if (!newPublicKey.isEmpty() && !newSecretKey.isEmpty()) {
                currentPublicKey = newPublicKey;
                keysBundle.addKeysPair(new KeysEntity(newPublicKey, newSecretKey));
                resetInputFields();
                savePreferences();
                refreshKeys();
            }
        });
    }

    private void setDeleteKeyButton() {
        TextView textView = findViewById(R.id.deleteKeyButton);
        textView.setOnClickListener(v -> {
            if (!Objects.equals(currentPublicKey, BuildConfig.REVOLUT_MERCHANT_API_KEY)) {
                keysBundle.removeKeysPairByPublicKey(currentPublicKey);
                currentPublicKey = BuildConfig.REVOLUT_MERCHANT_API_KEY;
                savePreferences();
                refreshKeys();
            } else {
                Toast.makeText(getApplicationContext(), "Default key cannot be deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPreferences() {
        // Initialize SharedPreferences and Gson
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Gson gson = new Gson();

        // Load the YourObject instance from SharedPreferences
        String json = sharedPreferences.getString("keysBundle", "");
        KeysBundle savedKeysBundle = gson.fromJson(json, KeysBundle.class);
        json = sharedPreferences.getString("lastUsedKey", "");
        String lastUsedKey = gson.fromJson(json, String.class);
        if (savedKeysBundle != null) {
            keysBundle = savedKeysBundle;
            currentPublicKey = lastUsedKey;
        }
    }

    @Override
    protected void onDestroy() {
        savePreferences();
        super.onDestroy();
    }

    private void savePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(keysBundle);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("keysBundle", json);
        editor.putString("lastUsedKey", currentPublicKey);
        editor.apply();
    }

    private void setKeysDropdown() {
        LinearLayout spinnerPlaceholder = findViewById(R.id.select_key_button);
        spinnerPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a list of options
                final String[] options = keysBundle.getPublicKeys().toArray(new String[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(MerchantKeyScreenActivity.this);
                builder.setTitle("Select an option");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the selected option, lastUsedKey changed
                        currentPublicKey = options[which];
                        savePreferences();
                        refreshKeys();
                    }
                });

                builder.show();
            }
        });
    }

    private void refreshKeys() {
        TextView textView = (TextView) findViewById(R.id.current_key);
        textView.setText(currentPublicKey);
    }

    private void initKeysBundle() {
        keysBundle = new KeysBundle(new ArrayList<>(List.of(new KeysEntity(BuildConfig.REVOLUT_MERCHANT_API_KEY, "hidden"))));
        loadPreferences();
        KeysEntity oldOne = keysBundle.getKeys().stream().filter(x -> Objects.equals(x.getSecretKey(), "hidden") && !Objects.equals(x.getPublicKey(), BuildConfig.REVOLUT_MERCHANT_API_KEY)).findFirst().orElse(null);
        if (oldOne != null) {
            keysBundle.removeKeysPair(oldOne);
            currentPublicKey = BuildConfig.REVOLUT_MERCHANT_API_KEY;
        }
        keysBundle.initDefaultPair(new KeysEntity(BuildConfig.REVOLUT_MERCHANT_API_KEY, "hidden"));
    }
}
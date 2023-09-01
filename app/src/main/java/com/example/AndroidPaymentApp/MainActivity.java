package com.example.AndroidPaymentApp;

import static com.revolut.revolutpay.api.RevolutPayKt.createController;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.AndroidPaymentApp.models.KeysBundle;
import com.example.AndroidPaymentApp.models.KeysEntity;
import com.example.AndroidPaymentApp.security.RSAEncryption;
import com.google.gson.Gson;
import com.revolut.revolutpay.api.OrderResultCallback;
import com.revolut.revolutpay.api.RevolutPay;
import com.revolut.revolutpay.api.RevolutPayEnvironment;
import com.revolut.revolutpay.api.RevolutPayExtensionsKt;
import com.revolut.revolutpay.ui.button.RevolutPayButton;
import com.revolut.revolutpay.ui.button.RevolutPayButtonController;
import com.revolut.revolutpayments.RevolutPayments;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private KeysEntity revolutApiKeys = null;
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            // This method will be executed when the server does not respond in 5 seconds
            showTimeoutMessage();
        }
    };

    private void showTimeoutMessage() {
        showMessage("Server request timeout. Try again later");
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        isLoadingRevolutApi = false;
    }


    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    String revolutOrderId;
    boolean isLoadingRevolutApi;
    PaymentSheet.CustomerConfiguration configuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadPreferences();
        setBackButton();
        setRevolutButton();
        setContinueButton();
//        setStripeButton();
//        setHandpointButton();
    }

    private void setBackButton() {
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setContinueButton() {

    }

//    private void setHandpointButton() {
//        Button handpointButton = findViewById(R.id.pay_hp);
//
//    }
//
//    private void setStripeButton() {
//        Button stripeButton = findViewById(R.id.pay_stripe);
//        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
//        stripeButton.setOnClickListener(v -> {
//            fetchStripeApi();
//            if (paymentIntentClientSecret != null) {
//                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
//                        new PaymentSheet.Configuration("Merchant Name 123", configuration));
//            } else
//                Toast.makeText(getApplicationContext(), "API Loading ...", Toast.LENGTH_SHORT).show();
//        });
//    }

    private void setRevolutButton() {
        isLoadingRevolutApi = false;
        revolutOrderId = null;
        String public_key = BuildConfig.REVOLUT_MERCHANT_API_KEY;
        if (revolutApiKeys != null)
            public_key = revolutApiKeys.getPublicKey();
        RevolutPayButton revolutButton = findViewById(R.id.revolut_pay_button);
        RevolutPay revolutPay = RevolutPayExtensionsKt.getRevolutPay(RevolutPayments.INSTANCE);
        Uri returnUri = new Uri.Builder().scheme("scheme1").authority("host").build();
        revolutPay.init(RevolutPayEnvironment.SANDBOX, returnUri, public_key, false, null);
        RevolutPayButtonController controller = createController(revolutButton);
        controller.setHandler(flow -> {
            if (revolutOrderId != null) {
                flow.setOrderToken(revolutOrderId);
                flow.attachLifecycle(this.getLifecycle());
                flow.continueConfirmationFlow();
            } else {
                if (!isLoadingRevolutApi) {
                    isLoadingRevolutApi = true;
                    try {
                        fetchRevolutApi(orderId -> {
                            isLoadingRevolutApi = false;
                            if (orderId != null) {
                                revolutOrderId = orderId;
                                flow.setOrderToken(revolutOrderId);
                                flow.attachLifecycle(getLifecycle());
                                flow.continueConfirmationFlow();
                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (isLoadingRevolutApi) {
                        Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            return null;
        });
        controller.setOrderResultCallback(new OrderResultCallback() {
            @Override
            public void onOrderCompleted() {
                revolutOrderId = null;
                onBackPressed();
            }

            @Override
            public void onOrderFailed(@NonNull Throwable throwable) {
                revolutOrderId = null;
                onBackPressed();
            }
        });

        // Find the EditText fields and add TextWatcher listeners
        EditText amountEditText = findViewById(R.id.amount);
        EditText descriptionEditText = findViewById(R.id.description);
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not used, but required to implement
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                revolutOrderId = null; // Set revolutOrderId to null when the amount changes
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used, but required to implement
            }
        });

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not used, but required to implement
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                revolutOrderId = null; // Set revolutOrderId to null when the description changes
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used, but required to implement
            }
        });


    }

    private interface ApiLoadCallback {
        void onApiLoaded(String revolutOrderId);
    }

    private void notValidNumber() {
        Toast.makeText(this, "Please, enter valid number", Toast.LENGTH_SHORT).show();
        isLoadingRevolutApi = false;
    }

    private void fetchRevolutApi(ApiLoadCallback callback) throws Exception {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.getCache().clear();

        boolean isSecretPresent = revolutApiKeys != null && !BuildConfig.REVOLUT_MERCHANT_API_KEY.equals(revolutApiKeys.getPublicKey());
        String url = BuildConfig.REVOLUT_API_URL;
        if (isSecretPresent) {
            url = BuildConfig.REVOLUT_API_KEYED_URL;
        }
        // Prepare the request data as key-value pairs
        int amount = 0;
        try {
            amount = (int) (Double.parseDouble(((EditText) findViewById(R.id.amount)).getText().toString()) * 100);
        } catch (Exception e) {
            notValidNumber();
            return;
        }
        if (amount <= 0) {
            notValidNumber();
            return;
        }
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String currency = "USD";

        String requestData = null;
        try {
            requestData = "amount=" + amount + "&currency=" + currency + "&description=" + URLEncoder.encode(description, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String finalRequestData = requestData;

        if (isSecretPresent) {
            // Encrypt the secretKey using the public key
            String base64EncryptedSecretKey = RSAEncryption.encrypt(revolutApiKeys.getSecretKey(), readPublicRSA());
            // Convert the encrypted secretKey to Base64
            // Modify the existing requestData to include the encrypted secretKey
            finalRequestData += "&encrypted_secret_key=" + URLEncoder.encode(base64EncryptedSecretKey, "UTF-8");
        }

        handler.postDelayed(timeoutRunnable, 7000);
        String data = finalRequestData;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        revolutOrderId = jsonObject.getString("public_id");
                        callback.onApiLoaded(revolutOrderId); // Notify the callback with the orderId
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Err", response);
                        Toast.makeText(this, "Ensure you entered right keys", Toast.LENGTH_SHORT).show();
                        isLoadingRevolutApi = false;
                    }
                    handler.removeCallbacks(timeoutRunnable);
                },
                error -> {
                    // Handle Volley error
                    error.printStackTrace();
                    handler.removeCallbacks(timeoutRunnable);
                    showMessage("Server request failed.");
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                if (!isSecretPresent) {
                    headers.put("AUTH", BuildConfig.REVOLUT_MERCHANT_API_KEY);
                } else {
                    headers.put("AUTH", BuildConfig.REVOLUT_AUTH_KEY);
                }
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return data.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        queue.getCache().remove(url);
    }


    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Toast.makeText(this, ((PaymentSheetResult.Failed) paymentSheetResult).getError().getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show();
        }
    }

    public void fetchStripeApi() {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.getCache().clear();
        String url = BuildConfig.STRIPE_API_URL;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        configuration = new PaymentSheet.CustomerConfiguration(
                                jsonObject.getString("customer"),
                                jsonObject.getString("ephemeralKey")
                        );
                        paymentIntentClientSecret = jsonObject.getString("paymentIntent");
                        PaymentConfiguration.init(getApplicationContext(), jsonObject.getString("publishableKey"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, Throwable::printStackTrace) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("authKey", BuildConfig.STRIPE_AUTH_KEY);
                return paramV;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        queue.getCache().remove(url);
    }

    private void loadPreferences() {

        try {
            // Initialize SharedPreferences and Gson
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            Gson gson = new Gson();

            // Load the YourObject instance from SharedPreferences
            String json = sharedPreferences.getString("keysBundle", "");
            KeysBundle savedKeysBundle = gson.fromJson(json, KeysBundle.class);
            json = sharedPreferences.getString("lastUsedKey", "");
            String lastUsedKey = gson.fromJson(json, String.class);
            if (savedKeysBundle != null) {
                revolutApiKeys = savedKeysBundle.getKeys().stream()
                        .filter(x -> x.getPublicKey().equals(lastUsedKey))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception ex) {
            revolutApiKeys = null;
        }
    }

    public String readPublicRSA() {
        try {
            // Get the InputStream for the raw resource
            InputStream publicKeyInputStream = getResources().openRawResource(R.raw.public_key);

            // Read the public key content from the InputStream
            InputStreamReader inputStreamReader = new InputStreamReader(publicKeyInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder publicKeyStringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                publicKeyStringBuilder.append(line);
            }

            // Close the streams
            bufferedReader.close();
            inputStreamReader.close();
            publicKeyInputStream.close();

            // Get the public key string
            String publicKeyStr = publicKeyStringBuilder.toString();
            Log.d("PublicKey", publicKeyStr); // Print the key for debugging
            // Now you have the public key string to use for encryption
            // ...
            return publicKeyStr.trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
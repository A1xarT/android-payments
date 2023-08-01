package com.example.AndroidPaymentApp;

import static com.revolut.revolutpay.api.RevolutPayKt.createController;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.revolut.revolutpay.api.OrderResultCallback;
import com.revolut.revolutpay.api.RevolutPay;
import com.revolut.revolutpay.api.RevolutPayEnvironment;
import com.revolut.revolutpay.api.RevolutPayExtensionsKt;
import com.revolut.revolutpay.ui.button.Controller;
import com.revolut.revolutpay.ui.button.RevolutPayButton;
import com.revolut.revolutpayments.RevolutPayments;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    String revolutOrderId;
    boolean isLoadingRevolutApi;
    PaymentSheet.CustomerConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRevolutButton();
//        setStripeButton();
//        setHandpointButton();
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
        RevolutPayButton revolutButton = findViewById(R.id.revolut_pay_button);
        RevolutPay revolutPay = RevolutPayExtensionsKt.getRevolutPay(RevolutPayments.INSTANCE);
        Uri returnUri = new Uri.Builder().scheme("scheme1").authority("host").build();
        revolutPay.init(RevolutPayEnvironment.SANDBOX, returnUri, BuildConfig.REVOLUT_MERCHANT_API_KEY, false, null);
        Controller controller = createController(revolutButton);
        controller.setHandler(flow -> {
            if (revolutOrderId != null) {
                flow.setOrderToken(revolutOrderId);
                flow.attachLifecycle(this.getLifecycle());
                flow.continueConfirmationFlow();
            } else {
                if (!isLoadingRevolutApi) {
                    isLoadingRevolutApi = true;
                    fetchRevolutApi(orderId -> {
                        isLoadingRevolutApi = false;
                        if (orderId != null) {
                            revolutOrderId = orderId;
                            flow.setOrderToken(revolutOrderId);
                            flow.attachLifecycle(getLifecycle());
                            flow.continueConfirmationFlow();
                        }
                    });
                    Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        });
        controller.setOrderResultCallback(new OrderResultCallback() {
            @Override
            public void onOrderCompleted() {
                revolutOrderId = null;
            }

            @Override
            public void onOrderFailed(@NonNull Throwable throwable) {
                revolutOrderId = null;
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

    private void fetchRevolutApi(ApiLoadCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.getCache().clear();
        String url = BuildConfig.REVOLUT_API_URL;
        // Prepare the request data as key-value pairs
        int amount = Integer.parseInt(((EditText) findViewById(R.id.amount)).getText().toString().replaceAll("[,.]", ""));
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String currency = "USD";

        String requestData = null;
        try {
            requestData = "amount=" + amount + "&currency=" + currency + "&description=" + URLEncoder.encode(description, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String finalRequestData = requestData;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        revolutOrderId = jsonObject.getString("public_id");
                        callback.onApiLoaded(revolutOrderId); // Notify the callback with the orderId
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Err", response);
                        Toast.makeText(this, "Json Exception", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Handle Volley error
                    error.printStackTrace();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                headers.put("AUTH", BuildConfig.REVOLUT_MERCHANT_API_KEY);
                return headers;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return finalRequestData.getBytes("UTF-8");
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
}
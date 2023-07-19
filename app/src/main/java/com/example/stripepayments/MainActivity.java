package com.example.stripepayments;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.revolut.revolutpay.api.RevolutPay;
import com.revolut.revolutpay.api.RevolutPayEnvironment;
import com.revolut.revolutpay.api.RevolutPayExtensionsKt;
import com.revolut.revolutpay.api.RevolutPayKt;
import com.revolut.revolutpay.data.RevolutPayService;
import com.revolut.revolutpay.ui.button.RevolutPayButton;
import com.revolut.revolutpayments.RevolutPayments;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {


    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchStripeApi();
        Button stripeButton = findViewById(R.id.pay_stripe);
        stripeButton.setOnClickListener(v -> {
            fetchStripeApi();
            if (paymentIntentClientSecret != null) {
                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                        new PaymentSheet.Configuration("Merchant Name 123", configuration));
            } else
                Toast.makeText(getApplicationContext(), "API Loading ...", Toast.LENGTH_SHORT).show();
        });
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        revolutButton();
    }
    private void revolutButton(){
        String merchantPublicKey = "pk_Ic0nbOyZOR4ZYaiK6TWPENch9un5pkBoIEl411bYrPWwwW7O";
        Uri returnUri = Uri.parse("return_uri_scheme://return_uri_host");
        RevolutPay revolutPay = RevolutPayExtensionsKt.getRevolutPay(RevolutPayments.INSTANCE);
        revolutPay.init(RevolutPayEnvironment.SANDBOX, returnUri , merchantPublicKey, true, null);
        RevolutPayButton revolutPayButton = findViewById(R.id.revolut_pay_button);
        // setting click handler

        RevolutPayKt.createController(revolutPayButton).setHandler(flow -> {
            flow.setOrderToken();
            flow.attachLifecycle(getLifecycle());
            flow.continueConfirmationFlow();
        });

    }
    private void makeOrderRevolut(){
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.getCache().clear();
        String url = "https://php-api-stripe.000webhostapp.com/php-server//revolut.php";
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
                paramV.put("authKey", "abc");
                return paramV;
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
        String url = "https://php-api-stripe.000webhostapp.com/php-server//index.php";
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
                paramV.put("authKey", "abc");
                return paramV;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        queue.getCache().remove(url);
    }
}
package com.example.stripepayments;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.paymentsheet.PaymentSheetResultCallback;

import org.json.JSONException;
import org.json.JSONObject;

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
        fetchApi();
        Button button = findViewById(R.id.pay_now);
        button.setOnClickListener(v -> {
            fetchApi();
            if (paymentIntentClientSecret != null) {
                paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret,
                        new PaymentSheet.Configuration("Merchant Name 123", configuration));
            } else
                Toast.makeText(getApplicationContext(), "API Loading ...", Toast.LENGTH_SHORT).show();
        });
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
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

    public void fetchApi() {
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
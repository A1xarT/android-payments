<?php
$curl = curl_init();
$secretApiKey = 'sk_cto-vpXtVEtkoP1yG8qXfq5pS793P4wDW-2UVf6hZD1KGCMUOqsfQYIqYtAX2q00';
$expectedPublicKey = 'pk_Ic0nbOyZOR4ZYaiK6TWPENch9un5pkBoIEl411bYrPWwwW7O';

// Check if the Authorization header is set
if (isset($_SERVER['HTTP_AUTH'])) {
  // Extract the API key from the Authorization header
  $apiKey = $_SERVER['HTTP_AUTH'];

  // Check if the API key matches the expected public API key
  if ($apiKey === $expectedPublicKey) {
    // API key is valid, proceed with processing the request
    curl_setopt_array(
      $curl,
      array(
        CURLOPT_URL => 'https://sandbox-merchant.revolut.com/api/1.0/orders',
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_ENCODING => '',
        CURLOPT_MAXREDIRS => 10,
        CURLOPT_TIMEOUT => 0,
        CURLOPT_FOLLOWLOCATION => true,
        CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
        CURLOPT_CUSTOMREQUEST => 'POST',
        CURLOPT_POSTFIELDS => '{
  "amount": 500,
  "currency": "GBP",
  "email": "example.customer@email.com"
}',
        CURLOPT_HTTPHEADER => array(
          'Content-Type: application/json',
          'Accept: application/json',
          'Authorization: Bearer ' . $secretApiKey
        ),
      )
    );

    $response = curl_exec($curl);

    curl_close($curl);
    echo $response;
  } else {
    // Invalid API key, reject the request
    http_response_code(401); // Unauthorized status code
    echo 'Invalid API key. Access denied.';
  }
} else {
  // Authorization header is missing, reject the request
  http_response_code(401); // Unauthorized status code
  echo 'Authorization header is missing. Access denied.';
}
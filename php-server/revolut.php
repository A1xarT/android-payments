<?php

$curl = curl_init();

$secretApiKey = 'sk_cto-vpXtVEtkoP1yG8qXfq5pS793P4wDW-2UVf6hZD1KGCMUOqsfQYIqYtAX2q00';

curl_setopt_array($curl, array(
  CURLOPT_URL => 'https://sandbox-merchant.revolut.com/api/1.0/orders',
  CURLOPT_RETURNTRANSFER => true,
  CURLOPT_ENCODING => '',
  CURLOPT_MAXREDIRS => 10,
  CURLOPT_TIMEOUT => 0,
  CURLOPT_FOLLOWLOCATION => true,
  CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
  CURLOPT_CUSTOMREQUEST => 'POST',
  CURLOPT_POSTFIELDS =>'{
  "amount": 500,
  "currency": "GBP",
  "email": "example.customer@email.com"
}',
  CURLOPT_HTTPHEADER => array(
    'Content-Type: application/json',
    'Accept: application/json',
    'Authorization: Bearer ' .$secretApiKey
  ),
));

$response = curl_exec($curl);

curl_close($curl);
echo $response;
<?php

$curl = curl_init();

curl_setopt_array($curl, array(
  CURLOPT_URL => 'https://sandbox-merchant.revolut.com/api/payments/64c118bc-e4df-a9c4-84c3-338a9b6da6f4',
  CURLOPT_RETURNTRANSFER => true,
  CURLOPT_ENCODING => '',
  CURLOPT_MAXREDIRS => 10,
  CURLOPT_TIMEOUT => 0,
  CURLOPT_FOLLOWLOCATION => true,
  CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
  CURLOPT_CUSTOMREQUEST => 'GET',
  CURLOPT_HTTPHEADER => array(
    'Accept: application/json',
    'Authorization: Bearer sk_cto-vpXtVEtkoP1yG8qXfq5pS793P4wDW-2UVf6hZD1KGCMUOqsfQYIqYtAX2q00'
  ),
));

$response = curl_exec($curl);

curl_close($curl);
echo $response;
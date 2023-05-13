<?php
require 'vendor/autoload.php';
if(isset($_POST['authKey']) && ($_POST['authKey'] == "abc")){
//if(1){
$stripe = new \Stripe\StripeClient('sk_test_51N6EPuAIFK7YivTKA19nLBJnZXgV6v60799PjpGEUtzHmBpcqDsCtIyHWMHjmatTq6Pb3S8UTQCKTCp5YRWjRCHX001p2tQR6r');

// Use an existing Customer ID if this is a returning customer.
$customer = $stripe->customers->create(
    [
        'name' => "Illia",
        'address' => [
            'line1' => 'Demo adress',
            'postal_code' => '200000',
            'city' => 'New York',
            'state' => 'NY',
            'country' => 'US',
        ]
    ]
);
$ephemeralKey = $stripe->ephemeralKeys->create([
  'customer' => $customer->id,
], [
  'stripe_version' => '2022-08-01',
]);
$paymentIntent = $stripe->paymentIntents->create([
  'amount' => 1099, // 10.99 
  'currency' => 'usd',
  'description' => 'Payment for some service',
  'customer' => $customer->id,
  'automatic_payment_methods' => [
    'enabled' => 'true',
  ],
]);

echo json_encode(
  [
    'paymentIntent' => $paymentIntent->client_secret,
    'ephemeralKey' => $ephemeralKey->secret,
    'customer' => $customer->id,
    'publishableKey' => 'pk_test_51N6EPuAIFK7YivTKmiwwOB5LUp0ZX0kl18KWCZFUHhWECG0URzpRAc6Blx4AvXi12YEcDLNSZRJ1ntAAKhHno7ot005ktQcU9u'
  ]
);
http_response_code(200);
} else echo "Not authorized";
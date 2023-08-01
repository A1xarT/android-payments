<?php

// Check if the 'AUTH' header is set in the request
$authHeader = $_SERVER['HTTP_AUTH'] ?? null;
if ($authHeader !== 'pk_Ic0nbOyZOR4ZYaiK6TWPENch9un5pkBoIEl411bYrPWwwW7O') {
    // Invalid authentication token
    $response = array("error" => "Invalid authentication token in the header.");
    echo json_encode($response);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Check if all the required parameters are present
    if (isset($_POST['amount']) && isset($_POST['currency']) && isset($_POST['description'])) {
        // Prepare the request data
        $requestData = array(
            'amount' => $_POST['amount'],
            'currency' => $_POST['currency'],
            'description' => $_POST['description']
        );

        // Convert the data to JSON format
        $jsonData = json_encode($requestData);

        // Initialize cURL
        $curl = curl_init();

        curl_setopt_array($curl, array(
            CURLOPT_URL => 'https://sandbox-merchant.revolut.com/api/1.0/orders',
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_ENCODING => '',
            CURLOPT_MAXREDIRS => 10,
            CURLOPT_TIMEOUT => 0,
            CURLOPT_FOLLOWLOCATION => true,
            CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
            CURLOPT_CUSTOMREQUEST => 'POST',
            CURLOPT_POSTFIELDS => $jsonData,
            CURLOPT_HTTPHEADER => array(
                'Content-Type: application/json',
                'Accept: application/json',
                'Authorization: Bearer sk_cto-vpXtVEtkoP1yG8qXfq5pS793P4wDW-2UVf6hZD1KGCMUOqsfQYIqYtAX2q00'
            ),
        ));

        // Execute the cURL request
        $response = curl_exec($curl);

        // Check for cURL errors
        if (curl_errno($curl)) {
            $error = array("error" => "cURL Error: " . curl_error($curl));
            echo json_encode($error);
            curl_close($curl);
            exit;
        }

        // Close cURL
        curl_close($curl);

        // Parse the Revolut API response as JSON
        $jsonData = json_decode($response, true);

        // Output the parsed JSON response
        echo json_encode($jsonData);

    } else {
        // Some required parameters are missing
        $response = array("error" => "Missing required parameters.");
        echo json_encode($response);
    }
} elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Get the 'order_id' from the incoming GET request
    $order_id = $_GET['order_id'] ?? null;

    // Check if 'order_id' is present in the request
    if ($order_id !== null) {
        // Initialize cURL
        $curl = curl_init();

        curl_setopt_array($curl, array(
            CURLOPT_URL => 'https://sandbox-merchant.revolut.com/api/1.0/orders/' . $order_id,
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

        // Execute the cURL request
        $response = curl_exec($curl);

        // Check for cURL errors
        if (curl_errno($curl)) {
            $error = array("error" => "cURL Error: " . curl_error($curl));
            echo json_encode($error);
            curl_close($curl);
            exit;
        }

        // Close cURL
        curl_close($curl);

        // Parse the Revolut API response as JSON
        $jsonData = json_decode($response, true);

        // Output the parsed JSON response
        echo json_encode($jsonData);
    } else {
        // 'order_id' is missing in the request
        $response = array("error" => "Missing 'order_id' parameter.");
        echo json_encode($response);
    }
} else {
    // Invalid request method
    $response = array("error" => "Invalid request method.");
    echo json_encode($response);
}

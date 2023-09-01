package com.example.AndroidPaymentApp.security;


import android.util.Base64;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;

public class RSAEncryption {
    public static String encrypt(String plainText, String publicKeyPEM) throws Exception {
        // Remove header, footer, and whitespace characters from PEM key
        String publicKeyPEMTrimmed = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode base64-encoded public key bytes
        byte[] publicKeyBytes = Base64.decode(publicKeyPEMTrimmed, Base64.DEFAULT);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Use PKCS1Padding
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT).replaceAll("\\s", "");
    }
}

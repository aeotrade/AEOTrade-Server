package com.aeotrade.provider.oauth;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class RSA2048Util {

    /**
     * Generate RSA 2048 key pair
     * @return KeyPair object containing public and private keys
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Initialize with 2048 bits
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * RSA 2048 signing (SHA1withRSA)
     * @param data Map of parameters to be signed
     * @param privateKey Private key in Base64 string format
     * @return Base64 encoded signature
     * @throws Exception
     */
    public static String rsaSign(Map<String, Object> data, String privateKey) throws Exception {
        // Sort the data by key
        Map<String, Object> sortedData = new TreeMap<>(data);

        // Build query string
        StringBuilder dataString = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedData.entrySet()) {
            if (dataString.length() > 0) {
                dataString.append("&");
            }
            dataString.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // Get PrivateKey object from Base64 string
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);

        // Sign the data
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privKey);
        signature.update(dataString.toString().getBytes());

        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    /**
     * Verify RSA 2048 signature (SHA1withRSA)
     * @param data Map of parameters that was signed
     * @param sign Base64 encoded signature to verify
     * @param publicKey Public key in Base64 string format
     * @return true if verification succeeds, false otherwise
     * @throws Exception
     */
    public static boolean rsaVerify(Map<String, Object> data, String sign, String publicKey) throws Exception {
        // Sort the data by key
        Map<String, Object> sortedData = new TreeMap<>(data);

        // Build query string
        StringBuilder dataString = new StringBuilder();
        for (Map.Entry<String, Object> entry : sortedData.entrySet()) {
            if (dataString.length() > 0) {
                dataString.append("&");
            }
            dataString.append(entry.getKey()).append("=").append(entry.getValue());
        }

        // Get PublicKey object from Base64 string
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        // Verify the signature
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(pubKey);
        signature.update(dataString.toString().getBytes());

        byte[] signatureBytes = Base64.getDecoder().decode(sign);
        return signature.verify(signatureBytes);
    }

    public static void main(String[] args) {
        try {
            // 1. Generate key pair (in real scenario, you would load existing keys)
            KeyPair keyPair = generateKeyPair();
            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            System.out.println("Private Key: " + privateKey);
            System.out.println("Public Key: " + publicKey);

            // 2. Prepare test data
            Map<String, Object> testData = new HashMap<>();
            testData.put("serviceFlowId", "F2310011420000001");
            testData.put("uniCode", "TEST123456");
            testData.put("mobile", "13800138000");
            testData.put("userId", "user123");

            // 3. Sign the data
            String signature = rsaSign(testData, privateKey);
            System.out.println("Signature: " + signature);

            // 4. Verify the signature
            boolean isValid = rsaVerify(testData, signature, publicKey);
            System.out.println("Signature valid: " + isValid);

            // 5. Test with modified data (should fail verification)
            Map<String, Object> modifiedData = new HashMap<>(testData);
            modifiedData.put("mobile", "13800138001"); // Change one value
            boolean isModifiedValid = rsaVerify(modifiedData, signature, publicKey);
            System.out.println("Modified data valid: " + isModifiedValid);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

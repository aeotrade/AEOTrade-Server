package com.aeotrade.provider.oauth;

import java.security.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;

public class SouthernWindowIntegration {

    // RSA Signing
    /**
     * RSA签名
     * @param data 请求的参数
     * @param privateKey 私钥
     * @return 签名字符串
     * @throws Exception
     */
    public static String rsaEnSign(Map<String, Object> data, PrivateKey privateKey) throws Exception {
        // Sort the data by key
        Map<String, Object> sortedData = new TreeMap<>(data);

        // Build query string
        String dataString = sortedData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // Sign the data
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(dataString.getBytes());

        byte[] digitalSignature = signature.sign();
        return Base64.encodeBase64String(digitalSignature);
    }

    // RSA Verification
    /**
     * 验证数据来源
     * @param data 请求数据
     * @param sign 签名
     * @param publicKey 公钥
     * @return 验证结果 (1: 成功, 0: 失败, -1: 错误)
     * @throws Exception
     */
    public static int rsaVerify(Map<String, Object> data, String sign, PublicKey publicKey) throws Exception {
        // Sort the data by key
        Map<String, Object> sortedData = new TreeMap<>(data);

        // Build query string
        String dataString = sortedData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // Verify the signature
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(dataString.getBytes());

        byte[] signatureBytes = Base64.decodeBase64(sign);
        return signature.verify(signatureBytes) ? 1 : 0;
    }

    // User Authorization API
    public static class AuthRequest {
        private Map<String, Object> payload;
        private String sign;

        // Constructor, getters and setters
        public AuthRequest(Map<String, Object> payload, PrivateKey privateKey) throws Exception {
            this.payload = payload;
            this.sign = rsaEnSign(payload, privateKey);
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public String getSign() {
            return sign;
        }
    }

    public static class AuthResponse {
        private int code;
        private String message;
        private AuthData data;
        private boolean success;

        // Getters and setters
        public boolean isSuccess() {
            return code == 200;
        }

        public static class AuthData {
            private List<ServiceUrl> urls;
            private String uniCode;
            private boolean firstLogin;

            // Getters and setters
        }

        public static class ServiceUrl {
            private String name;
            private String url;

            // Getters and setters
        }
    }

    // Intention Registration API
    public static class IntentionRequest {
        private String guid;
        private String busiCode;
        private String senderId;
        private String version;
        private Map<String, Object> payload;
        private String sign;

        // Constructor, getters and setters
        public IntentionRequest(String guid, String busiCode, String senderId, String version,
                                Map<String, Object> payload, PrivateKey privateKey) throws Exception {
            this.guid = guid;
            this.busiCode = busiCode;
            this.senderId = senderId;
            this.version = version;
            this.payload = payload;
            this.sign = rsaEnSign(payload, privateKey);
        }
    }

    public static class IntentionResponse {
        private int code;
        private String message;
        private IntentionData data;
        private boolean success;

        // Getters and setters
        public boolean isSuccess() {
            return code == 200;
        }

        public static class IntentionData {
            private String serviceFlowId;
            private String status;
            private String statusTime;
            private String statusDetail;

            // Getters and setters
        }
    }

    // Example usage
    public static void main(String[] args) {
        try {
            // 1. User Authorization Example
            Map<String, Object> authPayload = new HashMap<>();
            authPayload.put("serviceFlowId", "本次业务操作流水id");
            authPayload.put("uniCode", "南方电子口岸给对接方分配的唯一ID号");
            authPayload.put("mobile", "手机");
            authPayload.put("userId", "用户ID");
            authPayload.put("userName", "用户名称");
            authPayload.put("entName", "企业名称");
            authPayload.put("scCode", "企业统一社会信用代码");

            // In a real implementation, you would load the actual private key
            PrivateKey privateKey = loadPrivateKey(); // Implement this method
            AuthRequest authRequest = new AuthRequest(authPayload, privateKey);

            // Send request to /api/southern/auth
            // AuthResponse authResponse = sendAuthRequest(authRequest);

            // 2. Intention Registration Example
            Map<String, Object> intentionPayload = new HashMap<>();
            intentionPayload.put("serviceFlowId", "F2310011420000001");
            intentionPayload.put("orgName", "xxx");
            intentionPayload.put("orgCode", "xxx");
            intentionPayload.put("userId", "xxx");
            intentionPayload.put("userName", "xxxxx 有限公司");
            intentionPayload.put("contactAddress", "xxxxxxxxxxxxxxxxxx");
            intentionPayload.put("contactMan", "xxx");
            intentionPayload.put("contactTel", "xxx");
            intentionPayload.put("email", "xxx");
            intentionPayload.put("position", "xxx");
            intentionPayload.put("notes", "xxx");

            IntentionRequest intentionRequest = new IntentionRequest(
                    "166970C4-AF54-470A-87E4-D598463783A2",
                    "CMJT04",
                    "XXXXXX",
                    "1.0",
                    intentionPayload,
                    privateKey
            );

            // Send request to /api/southern/intention
            // IntentionResponse intentionResponse = sendIntentionRequest(intentionRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to load private key (implementation depends on your key storage)
    private static PrivateKey loadPrivateKey() throws Exception {
        // Implement key loading logic here
        return null;
    }
}

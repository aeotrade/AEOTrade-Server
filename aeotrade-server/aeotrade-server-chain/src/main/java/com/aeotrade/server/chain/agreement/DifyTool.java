package com.aeotrade.server.chain.agreement;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
public class DifyTool {
    @Value("${hmtx.agreement.dify-server.url}")
    private String difyServerUrl;
    @Value("${hmtx.agreement.dify-server.review-api-key}")
    private String difyServerReviewApiKey;
    @Value("${hmtx.agreement.dify-server.response-mode:blocking}")
    private String difyServerResponseMode;
    @Value("${hmtx.agreement.dify-server.user:test-user-id-001}")
    private String difyServerUser;

    private final RestTemplate difyRestTemplate;

    public DifyTool(RestTemplate difyRestTemplate) {
        this.difyRestTemplate = difyRestTemplate;
    }

    //内容审核
    public JSONObject contentAudit(String content) {
        try {
            // 准备请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + difyServerReviewApiKey);
            // 准备请求体
            JSONObject inputsJsonObject = new JSONObject();
            inputsJsonObject.put("input_text", content);
            JSONObject requestBodyJsonObject = new JSONObject();
            requestBodyJsonObject.put("inputs", inputsJsonObject);
            requestBodyJsonObject.put("response_mode", difyServerResponseMode);
            requestBodyJsonObject.put("user", difyServerUser);

            // 创建HttpEntity
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJsonObject.toString(), headers);
            // 发送POST请求
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(60000); // 设置连接超时时间为60秒
            factory.setReadTimeout(60*10*1000); // 设置读取超时时间为10分钟
            difyRestTemplate.setRequestFactory(factory);
            ResponseEntity<String> response = difyRestTemplate.postForEntity(difyServerUrl, requestEntity, String.class);
            // 打印响应状态码和响应体
            log.info("Dify Server Response Body: {}", response.getBody());

            return JSONObject.parseObject(response.getBody());
        } catch (Exception e) {
            log.warn("Dify Server Error: {}", e.getMessage());
            return null;
        }
    }

}

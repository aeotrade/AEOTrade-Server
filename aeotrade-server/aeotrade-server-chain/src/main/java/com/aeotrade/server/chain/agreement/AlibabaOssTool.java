package com.aeotrade.server.chain.agreement;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class AlibabaOssTool {
    @Value("${hmtx.agreement.oss-bucket-name:}")
    private String bucketName;
    @Value("${hmtx.agreement.oss-endpoint:}")
    private String endpoint ;
    @Value("${hmtx.agreement.oss-accessKeySecret:}")
    private String accessKeySecret ;
    @Value("${hmtx.agreement.oss-accessKeyId:}")
    private String  accessKeyId ;

    /**
     * 上传内容,并获取查看URL
     */
    public String uploadContent(String content){
        return uploadOss(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),UUID.randomUUID().toString() + ".txt","text/plain");
    }

    /**
     * 上传图片
     * @param inputStream 图片流
     */
    public String uploadImage(InputStream inputStream,String fileName){
        return uploadOss(inputStream,fileName,"image/png");
    }
    private String uploadOss(InputStream inputStream,String objectName,String contentType) {
        if (endpoint==null||accessKeyId==null||accessKeySecret==null||bucketName==null){
            throw new RuntimeException("Please initialize oss");
        }
        /**oss上传*/
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        ObjectMetadata metadata = new ObjectMetadata(); // 可以设置一些元数据，如Content-Type等，但这里不需要特别设置。
        metadata.setContentType(contentType);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream, metadata);
        ossClient.putObject(putObjectRequest);
        // 设置URL过期时间为100年  3600l* 1000*24*365*100
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 100);
        //获取url
        URL urls = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        ossClient.shutdown();
        log.info("内容地址：{}",urls.toString());
        return urls.toString();
    }

    //读取文件内容
    public String readUrlContent(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            log.warn("Error reading file content: " + e.getMessage());
            return null;
        }

    }
}

package com.aeotrade.provider.admin.controller;


import com.aeotrade.provider.admin.config.MqProperties;
import com.aeotrade.provider.admin.uacVo.MqApp;
import com.aeotrade.provider.admin.uacVo.weiXinVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class WeiXinController extends BaseController {

    @Autowired
    private MqProperties mqProperties;

    @Value("${hmtx.wx.mq.app-id:}")
    private String WX_APP_ID;
    @Value("${hmtx.wx.mq.secret:}")
    private String WX_SECRET;

    private final RestTemplate restTemplate;

    public WeiXinController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private MqApp getMqApp(String appName) {
        if (mqProperties == null || mqProperties.getConfigs() == null) {
            return null;
        }
        return mqProperties.getConfigs().stream().filter(mqApp -> mqApp.getAppName().equals(appName)).findFirst().orElse(null);
    }

    /**
     * 获取稳定版接口调用凭据
     * @return
     */
    @PostMapping("getStableAccessToken")
    public RespResult getStableAccessToken(String appName) {

        String url="https://api.weixin.qq.com/cgi-bin/stable_token";
        //有参post请求，参数为String
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map= new HashMap<>();
        map.put("grant_type", "client_credential");
        if (appName != null) {
            MqApp mqApp = getMqApp(appName);
            if (mqApp != null && mqApp.getAppId() != null && mqApp.getAppSecret() != null) {
                map.put("appid", mqApp.getAppId());
                map.put("secret", mqApp.getAppSecret());
            }else {
                map.put("appid", WX_APP_ID);
                map.put("secret", WX_SECRET);
            }
        }else {
            map.put("appid", WX_APP_ID);
            map.put("secret", WX_SECRET);
        }
//        map.add("force_refresh","false");
        String requestBody = JSON.toJSONString(map);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);


        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        Map<String,String> s = JSON.parseObject(responseEntity.getBody(),Map.class);

        return handleResult(s.get("access_token"));
    }

    /**
     * 手机号快速验证
     * @return
     */
    @PostMapping("getuserphonenumber")
    public RespResult getuserphonenumber(@RequestBody Map code){
        String accessToken=null;
        if (code.containsKey("appName")){
            accessToken=getStableAccessToken(code.get("appName").toString()).getResult().toString();
        }else {
            accessToken=getStableAccessToken(null).getResult().toString();
        }
        if (StringUtils.isBlank(accessToken)){
            return null;
        }
        String url="https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token="+accessToken;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> map= new HashMap<>();
        map.put("code", code.get("code").toString());
        String requestBody = JSON.toJSONString(map);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        weiXinVo s = JSON.parseObject(responseEntity.getBody(), weiXinVo.class);

        return handleResult(s.getPhone_info());
    }

    /**
     * 小程序登录
     * @param code
     * @return
     */
    @PostMapping("jscode2session")
    public RespResult jscode2session(@RequestBody Map code){
        String appId=null;
        String appSecret=null;
        if (code.containsKey("appName")){
            MqApp mqApp = getMqApp(code.get("appName").toString());
            if (mqApp != null && mqApp.getAppId() != null && mqApp.getAppSecret() != null) {
                appId=mqApp.getAppId();
                appSecret=mqApp.getAppSecret();
            }
        }else {
            appId=WX_APP_ID;
            appSecret=WX_SECRET;
        }

        String url="https://api.weixin.qq.com/sns/jscode2session?appid="+appId+"&secret="+appSecret+"&js_code="+code.get("code").toString()+"&grant_type=authorization_code";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        Map<String, String> map= new HashMap<>();
//        map.put("appid", WX_APP_ID);
//        map.put("secret", WX_SECRET);
//        map.put("js_code", code.get("code").toString());
//        map.put("grant_type", "authorization_code");

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        Map<String,String> s = JSON.parseObject(responseEntity.getBody(),Map.class);
        s.remove("session_key");
        return handleResult(s);
    }
}

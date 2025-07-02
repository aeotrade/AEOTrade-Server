package com.aeotrade.provider.util;

import com.aeotrade.dingding.SendTextMessage;
import com.aeotrade.service.MqSend;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件发送
 * @Author: yewei
 * @Date: 2020/3/6 14:24
 */
@Component
@Slf4j
public class MailService {

    @Autowired
    private MqSend mqSend;

    @Value("${spring.mail.username:}")
    private String username;
    @Value("${aeotrade.ops-mail-recipients:}")
    private String recipients;
    @Value("${aeotrade.environment:}")
    private String environment;
    @Value("${hmtx.dingding.secret:}")
    private String secret;
    @Value("${hmtx.dingding.token:}")
    private String token;
    @Value("${hmtx.lianjieqi:}")
    private String lianjieqi;

    /**
     *
     */
    @Async
    public void sendMail(MgLogEntity logEntity ) {
//        String recipient = recipients;
//        String[] serndEmial = recipient.split(",");
//        //正文
        String main =
                "运营人员:"+"\n" +
                        "您好!"+"\n"+
                        "慧贸OS收到新用户信息，请尽快跟进，祝成功" +"("+environment+")"+"\n" +
                        "时间："+logEntity.getTime()+"\n"+
                        "姓名："+logEntity.getName()+"\n"+
                        "电话: "+logEntity.getTel()+"\n" +
                        "来源: "+logEntity.getQudao()+"\n"+
                        "产品: "+logEntity.getApp()+ "\n";
        if (!StringUtils.isEmpty(secret)) {
            SendTextMessage.sendWithAtAll(main,token,secret);
        }

        HashMap<String, String> messageMap = new HashMap<>();
        messageMap.put("receive_id", logEntity.getStaffId());
        messageMap.put("receive_name", logEntity.getName());
        messageMap.put("receive_type", "1");
        messageMap.put("details_type", "2");
        messageMap.put("details_button", "了解更多");
        messageMap.put("message_details","https://help.aeotrade.com/5812/35f6");
        messageMap.put("message_source", "基础平台");
        messageMap.put("name", logEntity.getName());

        messageMap.put("template_number", "GG2024000005");
        mqSend.sendMessage(JSONObject.toJSONString(messageMap), "GG2024000005");
    }
    @Async
    public void sendFailMail(Map<String,Object> map) throws Exception {
//        String recipient = recipients;
//        String[] serndEmial = recipient.split(",");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
//        //正文
        String main =
                "运营人员:" + "\n" +
                        "您好!" + "\n" +
                        "慧贸OS收到新企业信息，请尽快跟进，祝成功" +"("+environment+")"+"\n" +
                        "时间：" + format + "\n" +
                        "企业名称：" + map.get("memberName") + "\n" +
                        "统一社会信用代码: " + map.get("uscCode") + "\n" +
                        "慧贸OS角色: " + map.get("role") + "\n" +
                        "联系人姓名: " + map.get("staffName") + "\n" +
                        "联系人手机号: " + map.get("mobile") + "\n";
        if (!StringUtils.isEmpty(secret)) {
            SendTextMessage.sendWithAtAll(main, token, secret);
        }
        if (!StringUtils.isEmpty(lianjieqi)) {
            HttpRequestUtils.httpPost(lianjieqi, map);
        }
    }
}

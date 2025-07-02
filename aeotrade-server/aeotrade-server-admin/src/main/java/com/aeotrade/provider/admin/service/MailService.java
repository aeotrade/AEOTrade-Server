package com.aeotrade.provider.admin.service;

import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.uacVo.SgsBankDto;
import com.aeotrade.dingding.SendTextMessage;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.utlis.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 邮件发送
 *
 * @Author: yewei
 * @Date: 2020/3/6 14:24
 */
@Component
@Lazy
@Slf4j
public class MailService {

    @Autowired
    private UacMemberService uacMemberMapper;
    @Value("${aeotrade.environment:}")
    private String environment;
    @Value("${hmtx.lianjieqi:}")
    private String lianjieqi;
    @Value("${hmtx.dingding.secret:}")
    private String secret;
    @Value("${hmtx.dingding.token:}")
    private String token;

    private static String rengzhengToken="148640a16f550822f5206b5a9c738b50434ca0bcbc132f71fdc4abe590d03297";

    private static String rengzhengSecret="SEC824e32f14944355ecf2ef825b1e82fc301b8fea6e716fe83572a338034ebe9bb";

    /**
     * @param uacMember
     */
    @Async
    public void sendMail(SgsBankDto uacMember, String[] serndEmail) {

        UacMember member = uacMemberMapper.getById(uacMember.getMemberId());
        //  StringUtils.isEmpty()
        if (StringUtils.isEmpty(uacMember.getMemberName()) && StringUtils.isEmpty(uacMember.getUscc())
                && StringUtils.isEmpty(member.getStaffName()) && StringUtils.isEmpty(member.getStasfTel())
                && StringUtils.isEmpty(uacMember.getBankAccount()) && StringUtils.isEmpty(uacMember.getBankOfDeposit())
                && uacMember.getDeduction() == null) {
            throw new AeotradeException("数据缺失");
        } else {
            //正文
            String main = "环境:" + environment + "\n" +
                    "企业已完成银行对公账户认证" + "\n" +
                    "组织名称:" + uacMember.getMemberName() + "\n" +
                    "社会统一信用代码:" + uacMember.getUscc() + "\n" +
                    "组织联系人:" + member.getStaffName() + "\n" +
                    "手机号号码:" + member.getStasfTel() + "\n" +
                    "银行开户名:" + uacMember.getBankAccountName() + "\n"+
                    "开户银行:" + uacMember.getBankOfDeposit() + "\n"+
                    "开户支行/营业部:" + uacMember.getBankSub() + "\n"+
                    "银行账号:" + uacMember.getBankAccount() + "\n" +
                    "打款金额:" + uacMember.getDeduction() + "\n";
            try {
                if (!org.springframework.util.StringUtils.isEmpty(secret)) {
                    SendTextMessage.sendWithAtAll(main, rengzhengToken, rengzhengSecret);
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
                throw new AeotradeException("邮件发送失败");
            }
            System.out.println(Arrays.toString(serndEmail) + ":邮件发送完毕");

        }
    }

    /**
     * @param uacMember
     */
    @Async
    public void sendMemberMail(SgsCertInfo uacMember, String[] serndEmail) {

        UacMember member = uacMemberMapper.getById(uacMember.getMemberId());
        //  StringUtils.isEmpty()
        if (StringUtils.isEmpty(uacMember.getMemberName()) && StringUtils.isEmpty(uacMember.getUscc())
                && StringUtils.isEmpty(member.getStaffName()) && StringUtils.isEmpty(member.getStasfTel())) {
            throw new AeotradeException("数据缺失");
        } else {
            //正文
            String main = "环境:" + environment + "\n" +
                    "企业已提交实名认证申请" + "\n" +
                    "企业名称:" + uacMember.getMemberName() + "\n" +
                    "社会统一信用代码:" + uacMember.getUscc() + "\n" +
                    "企业联系人:" + member.getStaffName() + "\n" +
                    "联系人电话:" + member.getStasfTel() + "\n";
            try {
                if (!org.springframework.util.StringUtils.isEmpty(secret)) {
                    SendTextMessage.sendWithAtAll(main, token, secret);
                }
            } catch (Exception e) {
                log.warn(e.getMessage());
                throw new AeotradeException("邮件发送失败");
            }
            System.out.println(Arrays.toString(serndEmail) + ":邮件发送完毕");

        }
    }


    @Async
    public void sendFailMail(Map<String, Object> map) throws Exception {
//        String recipient = recipients;
//        String[] serndEmial = recipient.split(",");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        //正文
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
        if (!org.springframework.util.StringUtils.isEmpty(secret)) {
            SendTextMessage.sendWithAtAll(main, token, secret);
        }
        if (!org.springframework.util.StringUtils.isEmpty(lianjieqi)) {
            HttpRequestUtils.httpPost(lianjieqi, map);
        }
    }
}

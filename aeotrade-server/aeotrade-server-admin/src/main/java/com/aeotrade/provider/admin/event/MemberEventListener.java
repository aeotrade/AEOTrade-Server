package com.aeotrade.provider.admin.event;

import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.entiy.UawVipType;
import com.aeotrade.provider.admin.service.MailService;
import com.aeotrade.provider.admin.uacVo.SgsBankDto;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MemberEventListener {

    private final MailService mailService;

    public MemberEventListener(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * 向运营人员发送处理邮件
     * @param event
     * @throws Exception
     */
    @Async("eventExecutor")
    @EventListener
    public void sendMail(AddMemberEvent event) throws Exception {
        //暂时添加判断以适应项目中剔除了Mail的逻辑
        if (mailService == null) {
            return;
        }
        UacMember uacMember=event.getUacMember();
        UawVipType vipType=event.getVipType();
        Map<String, Object> map = new HashMap<>();
        map.put("memberName", uacMember.getMemberName());
        map.put("uscCode", uacMember.getUscCode());
        map.put("staffName", uacMember.getStaffName());
        map.put("mobile", uacMember.getStasfTel());
        map.put("role", vipType.getTypeName());
        map.put("memberId", uacMember.getId());
        map.put("userId", uacMember.getStaffId());
        mailService.sendFailMail(map);
    }

    /**
     * 用户银行卡认证
     * @param event
     */
    @Async("eventExecutor")
    @EventListener
    public void memberBankAuthentication(MemberBankAuthenticationEvent event) {
        //暂时添加判断以适应项目中剔除了Mail的逻辑
        if (mailService == null) {
            return;
        }
        SgsBankDto sgsBankDto=event.getSgsBankDto();
        String[] recipient = event.getRecipient();
        mailService.sendMail(sgsBankDto, recipient);
    }
    /**
     * 用户证件认证
     * @param event
     */
    @Async("eventExecutor")
    @EventListener
    public void memberCertAuthentication(MemberCertAuthenticationEvent event) {
        //暂时添加判断以适应项目中剔除了Mail的逻辑
        if (mailService == null) {
            return;
        }
        SgsCertInfo sgsCertInfo=event.getSgsCertInfo();
        String[] recipient = event.getRecipient();
        mailService.sendMemberMail(sgsCertInfo, recipient);
    }
}

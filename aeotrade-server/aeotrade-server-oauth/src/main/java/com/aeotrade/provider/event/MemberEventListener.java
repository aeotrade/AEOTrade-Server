package com.aeotrade.provider.event;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.provider.util.MailService;
import com.aeotrade.provider.util.MgLogEntity;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
    @Async(AeoConstant.ASYNC_POOL)
    @EventListener
    public void singleLoginMail(AddMemberEvent event) throws Exception {
        //暂时添加判断以适应项目中剔除了Mail的逻辑
        if (mailService == null) {
            return;
        }
        Map<String, Object> map = event.getMap();
        mailService.sendFailMail(map);
    }

    /**
     * 运营跟进通知
     * @param event
     */
    @Async(AeoConstant.ASYNC_POOL)
    @EventListener
    public void memberBankAuthentication(MemberAuthEvent event) {
        //暂时添加判断以适应项目中剔除了Mail的逻辑
        if (mailService == null) {
            return;
        }
        MgLogEntity entity = event.getEntity();
        mailService.sendMail(entity);
    }

}

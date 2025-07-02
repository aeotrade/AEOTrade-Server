package com.aeotrade.server.log.config;

import com.aeotrade.server.log.service.impl.AeotradeUserLogInfoServicelmpl;
import com.aeotrade.server.log.service.impl.UserLogInfoServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;

/**
 * @Auther: 吴浩
 * @Date: 2022-10-24 11:32
 */
@Slf4j
@EnableBinding(MqLogReceiveConfig.class)
public class ReceiveLog {

    private final UserLogInfoServiceImpl userLogInfoService;

    private final AeotradeUserLogInfoServicelmpl aeotradeUserLogInfoServicelmpl;

    public ReceiveLog(UserLogInfoServiceImpl userLogInfoService, AeotradeUserLogInfoServicelmpl aeotradeUserLogInfoServicelmpl) {
        this.userLogInfoService = userLogInfoService;
        this.aeotradeUserLogInfoServicelmpl = aeotradeUserLogInfoServicelmpl;
    }

    /**
     * 慧贸平台用户操作日志消息处理
     */
    @StreamListener(MqLogReceiveConfig.LOG_STAT)
    public void receiveLOG_STAT(Message<String> message) {
        try {
            String payload = message.getPayload();
             userLogInfoService.insertUserLog(payload);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    /**
     * 全平台用户操作日志消息处理
     */
    @RabbitListener(queuesToDeclare = @Queue("AEO_LOG_STAT"))
    public void receiveAEO_LOG_STAT(Message<String> message) {
        try {
            String payload = message.getPayload();
            aeotradeUserLogInfoServicelmpl.insertAeotradeUserLog(payload);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    @RabbitListener(queuesToDeclare = @Queue("LOG_STAT_RPA"))
    public void receiveLOG_STAT_RPA(Message<String> message) {
        try {
            String payload = message.getPayload();
            userLogInfoService.insertDo(payload);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }



    @RabbitListener(queuesToDeclare = @Queue("LOG_STAT_ADMIN"))
    public void receiveLOG_STAT_ADMIN(Message<String> message) {
        try {
            String payload = message.getPayload();
            userLogInfoService.insertAdminLog(payload);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}

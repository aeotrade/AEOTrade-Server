package com.aeotrade.server.message.service.impl;

import com.aeotrade.server.message.config.MqReceiveConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

/**
 * 接收MQ消息
 * @Author: yewei
 * @Date: 11:20 2021/2/19
 * @Description:
 */
@Slf4j
@EnableBinding(MqReceiveConfig.class)
public class ReceiveMessage {
    @Autowired
    private MsgMessageServiceImpl msgMessageService;

    /**消息中心接收通道*/
    @StreamListener(MqReceiveConfig.MESSAGESERVICE)
    public void saveOrderMessage(Message<String> message) {
        log.info("接到的消息为: {}",message.getPayload());
        msgMessageService.receivceMessage(message.getPayload());
    }

    /**
     * 通知类消息失败处理
     */
    @ServiceActivator(inputChannel = "queue.mamber.mambergroup.errors")
    public void error(Message<String> message) {
        log.warn("Message consumer failed, call fallback!");
    }



}

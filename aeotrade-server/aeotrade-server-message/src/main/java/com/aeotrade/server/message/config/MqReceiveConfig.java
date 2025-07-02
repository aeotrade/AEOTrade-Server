package com.aeotrade.server.message.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 10:58 2021/2/19
 * @Description:
 */


@Component
public interface MqReceiveConfig {

    /**接收通知类消息*/
    String MESSAGESERVICE = "messageservice";
    @Input(MESSAGESERVICE)
    SubscribableChannel receiveInform();

}

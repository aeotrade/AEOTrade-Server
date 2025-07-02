package com.aeotrade.server.log.config;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 10:58 2021/2/19
 * @Description:
 */


@Component
public interface MqLogReceiveConfig {

    String LOG_STAT = "LOG_STAT";
    @Input(LOG_STAT)
    MessageChannel receiveLOG_STAT();

}

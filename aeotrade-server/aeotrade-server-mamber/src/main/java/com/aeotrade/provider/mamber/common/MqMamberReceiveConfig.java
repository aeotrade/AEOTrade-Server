package com.aeotrade.provider.mamber.common;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 10:58 2021/2/19
 * @Description:
 */


@Component
public interface MqMamberReceiveConfig {

    String MAMBERSERVICE = "mamberservice";
    @Input(MAMBERSERVICE)
    MessageChannel receivePay();

}

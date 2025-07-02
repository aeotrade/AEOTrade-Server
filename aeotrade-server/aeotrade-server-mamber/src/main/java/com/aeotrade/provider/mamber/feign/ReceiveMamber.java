package com.aeotrade.provider.mamber.feign;

import com.aeotrade.provider.mamber.common.MqMamberReceiveConfig;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.vo.MessageVo;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;

import java.time.LocalDateTime;

/**
 * 接收MQ消息
 * @Author: yewei
 * @Date: 11:20 2021/2/19
 * @Description:
 */
@Slf4j
@EnableBinding(MqMamberReceiveConfig.class)
public class ReceiveMamber {
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;

    /**通知类消息处理*/
    @StreamListener(MqMamberReceiveConfig.MAMBERSERVICE)
    //@Ex(value = "会员购买后开通信息处理")
    public void saveOrderMessage(Message<String> message) throws Exception {

        System.out.println( message.getPayload());
        MessageVo vo = JSON.parseObject(message.getPayload(), MessageVo.class);
        System.out.println(vo+"+++++++++++++++");
        uawVipMessageService.updateMessage(vo.getUserId(),vo.getVipclass(),vo.getApply(),
                vo.getGoodsCategoryId(), LocalDateTime.parse(vo.getEndtime()),vo.getType());
    }

    /**
     * 通知类消息失败处理
     */
    @ServiceActivator(inputChannel = "queue.mamber.mambergroup.errors")
    public void error(Message<String> message) {
        System.out.println("Message consumer failed, call fallback!");
    }



}

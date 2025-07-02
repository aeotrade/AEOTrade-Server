package com.aeotrade;

import com.aeotrade.annotaion.BaseJSONResource;
import com.aeotrade.annotation.EnableMonitor;
import com.aeotrade.server.log.config.MqLogReceiveConfig;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableDiscoveryClient
@EnableMonitor
@MapperScan({"com.aeotrade.server.message.mapper","com.aeotrade.server.log.mapper"})
@ComponentScan(basePackages = {"com.aeotrade.*"})
//log
@EnableBinding(value = {MqLogReceiveConfig.class})
@EnableScheduling
@EnableFeignClients
@BaseJSONResource//全局JSON处理
@Slf4j
public class AeotradeServerMessage {

    public static void main(String[] args) {

        try {
            SpringApplication.run(AeotradeServerMessage.class, args);
            log.info("\n __________                    .__                \n"
                    + "\\______   \\__ __  ____   ____ |__| ____    ____  \n"
                    + " |       _/  |  \\/    \\ /    \\|  |/    \\  / ___\\ \n"
                    + " |    |   \\  |  /   |  \\   |  \\  |   |  \\/ /_/  >\n"
                    + " |____|_  /____/|___|  /___|  /__|___|  /\\___  / \n"
                    + "      \\/   启动成功  \\/     \\/ 消息日志系统 \\//_____/  ");
        } catch (RuntimeException re) {
            log.debug(re.getMessage());
        }

    }

}

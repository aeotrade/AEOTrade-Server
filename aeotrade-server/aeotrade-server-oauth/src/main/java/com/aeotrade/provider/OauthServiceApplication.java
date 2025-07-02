package com.aeotrade.provider;

import com.aeotrade.configure.HmmMonitorConfigure;
import com.aeotrade.service.MqSend;
import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
@SpringBootApplication(exclude = HmmMonitorConfigure.class)
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.aeotrade.provider.mapper")
@EnableBinding(value ={MqSend.class})
//@ComponentScan({"com.aeotrade.*"})
@Slf4j
public class OauthServiceApplication {
    public static void main(String[] args) {
        try {

        SpringApplication.run(OauthServiceApplication.class, args);
        log.info("\n__________                    .__                \n"
                + "\\______   \\__ __  ____   ____ |__| ____    ____  \n"
                + " |       _/  |  \\/    \\ /    \\|  |/    \\  / ___\\ \n"
                + " |    |   \\  |  /   |  \\   |  \\  |   |  \\/ /_/  >\n"
                + " |____|_  /____/|___|  /___|  /__|___|  /\\___  / \n"
                + "    \\/   启动成功  \\/     \\/ 慧贸OS登录鉴权系统 \\//_____/  ");

    } catch (RuntimeException re) {
        log.warn(re.getMessage());
    }}

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

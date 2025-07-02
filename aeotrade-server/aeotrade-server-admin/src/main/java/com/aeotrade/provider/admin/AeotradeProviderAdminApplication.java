package com.aeotrade.provider.admin;

import com.aeotrade.annotation.EnableHmtxCloudResourceServer;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.aeotrade.provider.admin.mapper")
@ComponentScan(basePackages = {"com.aeotrade.*"})
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@EnableHmtxCloudResourceServer
@Slf4j
public class AeotradeProviderAdminApplication {

    public static void main(String[] args) {
        try {
        SpringApplication.run(AeotradeProviderAdminApplication.class, args);
        log.info("\n__________                    .__                \n" +
                "\\______   \\__ __  ____   ____ |__| ____    ____  \n" +
                " |       _/  |  \\/    \\ /    \\|  |/    \\  / ___\\ \n" +
                " |    |   \\  |  /   |  \\   |  \\  |   |  \\/ /_/  >\n" +
                " |____|_  /____/|___|  /___|  /__|___|  /\\___  / \n" +
                "        \\/   启动成功  \\/     \\/ 用户运营系统 \\//_____/  ");
        } catch (RuntimeException re) {
            log.warn(re.getMessage());
        }}
}

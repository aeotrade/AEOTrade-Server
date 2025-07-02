package com.aeotrade;

import com.aeotrade.annotation.EnableHmtxCloudResourceServer;
import com.aeotrade.configure.HmtxLog;
import com.aeotrade.provider.file.upload.property.FileProperties;
import com.aeotrade.provider.mamber.common.MqMamberReceiveConfig;
import feign.Logger;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ®
 * 应用启动入口
 * Created by hmm on 2018/4/26.
 */

@SpringBootApplication
@MapperScan("com.aeotrade.provider.mamber.mapper")
@EnableBinding(value ={MqMamberReceiveConfig.class})
@EnableAsync
@EnableConfigurationProperties(FileProperties.class)
@EnableScheduling
@EnableHmtxCloudResourceServer
@EnableFeignClients
@HmtxLog
@EnableDiscoveryClient
//@ComponentScan({"com.aeotrade.*"})
public class MamberServiceApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(MamberServiceApplication.class, args);
            System.out.println("__________                    .__                \n"
                    + "\\______   \\__ __  ____   ____ |__| ____    ____  \n"
                    + " |       _/  |  \\/    \\ /    \\|  |/    \\  / ___\\ \n"
                    + " |    |   \\  |  /   |  \\   |  \\  |   |  \\/ /_/  >\n"
                    + " |____|_  /____/|___|  /___|  /__|___|  /\\___  / \n"
                    + "      \\/   启动成功  \\/     \\/ 慧贸OS业务系统 \\//_____/  ");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }


    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}

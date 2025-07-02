package com.aeotrade.server;

import com.aeotrade.annotaion.BaseJSONResource;
import com.aeotrade.configure.HmmMonitorConfigure;
import com.aeotrade.configure.HmtxLog;
import com.aeotrade.utlis.SpringContextUtils;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, HmmMonitorConfigure.class})
@BaseJSONResource//全局JSON处理
@HmtxLog
@EnableDiscoveryClient
@Import(value = SpringContextUtils.class)
public class AeotradeServerGatewayApplication {
    public static void main(String[] args) {
        try {
            new SpringApplicationBuilder(AeotradeServerGatewayApplication.class)
                    .web(WebApplicationType.REACTIVE)
                    .run(args);
        } catch (Exception e) {
            e.getMessage();
            System.out.println(e.getMessage());

        }

    }


}

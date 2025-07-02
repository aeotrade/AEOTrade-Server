package com.aeotrade.server.chain;

import com.aeotrade.server.chain.config.MqChainReceiveConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableBinding(value ={MqChainReceiveConfig.class})
@ComponentScan(basePackages = {"com.aeotrade.*"})
@EnableScheduling
@EnableDiscoveryClient
//@EnableMongoRepositories(basePackages = {"com.aeotrade.chainmaker"})
public class AeotradeChainApplication {
    public static void main(String[] args) {
        SpringApplication.run(AeotradeChainApplication.class,args);
    }
}
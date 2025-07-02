package com.aeotrade.provider.admin.config;

import com.aeotrade.validator.SmsService;
import com.aeotrade.validator.aliyun.AliyunSmsServiceImpl;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@MapperScan("com.aeotrade.provider.admin.mapper")
public class UacProviderConfig {

    @Bean
    public SmsService smsService(){
        return new AliyunSmsServiceImpl();
    }
}

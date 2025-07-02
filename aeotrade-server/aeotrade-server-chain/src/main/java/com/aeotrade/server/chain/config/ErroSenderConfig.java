//package com.aeotrade.server.chain.config;
//
//
//import com.aeotrade.monitor.ErrorSender;
//import com.aeotrade.monitor.MethodMonitor;
//import com.aeotrade.properties.HmmMonitorMailProperties;
//import com.aeotrade.service.RedisService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.mail.javamail.JavaMailSender;
//
//import java.util.concurrent.TimeUnit;
//
//
//
///**
/// * @Author: yewei
/// * @Date: 15:30 2022-01-20
/// * @Description:
/// */
//
//
//
//@Configuration
//public class ErroSenderConfig {
//    @Autowired
//    private RedisService redisService;
//
//    @Autowired
//    private JavaMailSender javaMailSender;
//    @Bean
//    @Primary
//    public ErrorSender errorSender(){
//        MethodMonitor methodMonitor = new MethodMonitor(2, TimeUnit.DAYS);
//        ErrorSender monitorSender = new ErrorSender(methodMonitor,redisService,javaMailSender);
//        return monitorSender;
//    }
//}
//

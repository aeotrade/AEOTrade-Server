package com.aeotrade.provider.oauth.config;


import com.mongodb.MongoClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongodbConfig {
    @Bean
    public MongoClientSettings mongoClientOptions(){
        // 设置连接闲置时间，当超过这个闲置时间客户端主动关闭连接，下次使用时重新建立连接，这样可以有效避免连接失效的问题。
        return MongoClientSettings.builder().applyToSocketSettings(builder -> {
                    builder.connectTimeout(10, TimeUnit.SECONDS)  // 连接超时
                            .readTimeout(30, TimeUnit.SECONDS);    // 读取超时
                })
                .build();
    }
}

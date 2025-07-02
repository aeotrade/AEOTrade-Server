package com.aeotrade.server.chain.config;

import com.mongodb.client.MongoClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * @Author yewei
 * @Date 2022/6/1 11:33
 * @Description:
 * @Version 1.0
 */

@Configuration
/*@ConditionalOnProperty(value = "", havingValue = "", matchIfMissing = true)*/
@ConditionalOnExpression("!T(org.apache.commons.lang3.StringUtils).isNotEmpty('${spring.data.mongodb.primary:}')")
@EnableMongoRepositories(basePackages = "com.aeotrade.chainmaker.repository")
public class MongodbConfig  {



}

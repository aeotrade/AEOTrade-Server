package com.aeotrade.server.encryptor;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.EnvironmentPBEConfig;
import org.jasypt.iv.IvGenerator;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class RegisterTextEncryptorListener implements ApplicationListener<ApplicationPreparedEvent>, Ordered {
    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String password = environment.getProperty("jasypt.encryptor.password");
        String algorithm = environment.getProperty("jasypt.encryptor.algorithm");
        String classname = environment.getProperty("jasypt.encryptor.iv-generator-classname");
        // 创建解密器实例
        var encryptor = new StandardPBEStringEncryptor();
        var config = new EnvironmentPBEConfig();
        config.setPassword(StringUtils.isEmpty(password)?"pkslow":password);
        config.setAlgorithm(StringUtils.isEmpty(algorithm)?"PBEWithMD5AndDES":algorithm);
        try {
            Class<?> clazz = Class.forName(StringUtils.isEmpty(classname)?"org.jasypt.iv.NoIvGenerator":classname);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            config.setIvGenerator((IvGenerator) constructor.newInstance());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        encryptor.setConfig(config);
        if (applicationContext instanceof AnnotationConfigApplicationContext) {
            ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
            if (!beanFactory.containsBean("textEncryptor")) {
                beanFactory.registerSingleton("textEncryptor", new TextEncryptor() {
                    @Override
                    public String encrypt(String s) {
                        return encryptor.encrypt(s);
                    }

                    @Override
                    public String decrypt(String s) {
                        return encryptor.decrypt(s);
                    }
                });
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

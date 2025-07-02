package com.aeotrade.provider.admin.config;

import com.aeotrade.provider.admin.uacVo.MqApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "hmtx.wx.mq")
public class MqProperties {
    private List<MqApp> configs;
}

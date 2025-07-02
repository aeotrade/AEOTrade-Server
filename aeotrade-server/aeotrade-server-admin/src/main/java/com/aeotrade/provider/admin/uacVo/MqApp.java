package com.aeotrade.provider.admin.uacVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqApp {
    private String appName;
    private String appId;
    private String appSecret;
}

package com.aeotrade.server.chain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hmtx.chain")
public class IssuingAgenciesProperties {
    private BankAuth bankAuth;
    private BjsinglewindowAuth bjsinglewindowAuth;
}
//银行卡认证VC模板
@Data
class BankAuth{
    private String vcTemplateId;
    private String credentialName;
    private String issuerId;
    private String issuerName;
}
//北京单一窗口认证VC模板
@Data
class BjsinglewindowAuth{
    private String vcTemplateId;
    private String credentialName;
    private String issuerId;
    private String issuerName;
}
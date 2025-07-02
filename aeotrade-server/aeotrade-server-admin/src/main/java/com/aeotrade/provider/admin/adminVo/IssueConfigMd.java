package com.aeotrade.provider.admin.adminVo;

import lombok.Data;

@Data
public class IssueConfigMd {
    private Long sgsConfigurationId;
    private Long issuerCertId;
    private String issuerName;
    private String vcTemplateId;
    private String credentialName;
}

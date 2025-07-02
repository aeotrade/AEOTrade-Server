package com.aeotrade.provider.admin.uacVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuerConfigVO {
    //签发机构DID
    private String issuerId;
    //签发机构名称
    private String credentialName;
    //VC模版ID
    private String vcTemplateId;
    // 机构名称
    private String issuerName;
}

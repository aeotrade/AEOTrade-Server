package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.SgsConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=true)
public class SgsConfigurationVO extends SgsConfiguration implements Serializable {

    //@ApiModelProperty(value="认证时间", allowEmptyValue=true)
    private LocalDateTime sgsTime;
    //@ApiModelProperty(value="认证状态", allowEmptyValue=true)
    private Integer sgsUserStatus;

    private String remark;
    //签发机构DID
    private String issuerId;
    //签发机构名称
    private String credentialName;
    //VC模版ID
    private String vcTemplateId;
    // 机构名称
    private String issuerName;
}

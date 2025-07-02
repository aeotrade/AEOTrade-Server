package com.aeotrade.provider.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-19 13:36
 */
@Data
public class SingleClass {
    //认证接口的code
   private String code;
   //要入驻的工作台id
   private String workMark;
   //要认证的企业id
   private String memberId;
   //申请VC资质
   private String vcTemplateId;
   private String credentialName;
   private String issuerId;
   private String issuerName;
   private String staffId;
   private String sgsConfigId;
}

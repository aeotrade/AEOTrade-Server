package com.aeotrade.server.chain.config;

import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2022-05-26 9:34
 */
@Data
public class MessageVo implements Serializable {
    private String tenantId;
    private String tenantName;
    private String uscc;
    private String creatTime;
    private String userType;
    private String userId;
    private String roleCodeRulesEnum;
    private String chainId;
    private String userTypeEnum;
}

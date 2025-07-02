package com.aeotrade.server.chain.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2022-05-26 14:01
 */
@Data
public class ChainCertUserMemberVO {
    private String userType;
    private Date creatTime;
    private String orgId;
    private String organName;
    private String did;
    private Map<String,String> stringStringMap;
}

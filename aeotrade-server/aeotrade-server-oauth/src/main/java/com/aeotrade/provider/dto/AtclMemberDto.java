package com.aeotrade.provider.dto;

import lombok.Data;

/**
 * @Author: yewei
 * @Date: 2020/5/6 18:16
 */
@Data
public class AtclMemberDto {

    private String subscriberUscc;
    private String subscriberCode;
    private String subscriberName;
    /**法人名称*/
    private String legalPerson;
    /**法人邮箱*/
    private String legalPersonEmail;
    /**法人移动电话*/
    private String legalPersonMobile;
    /**法人座机*/
    private String legalPersonTel;
    private Long staffId;
}

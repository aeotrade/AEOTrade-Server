package com.aeotrade.provider.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-19 13:31
 */
@Data
public class SingleAttributes {
    private String credentialType;
    //企业名
    private String etps_name;
    //登录名称
    private String login_name;
    private String cus_reg_no;
    //手机号
    private String mobile;
    //个人用户名称
    private String op_name;
    //显示名
    private String show_name;
    //企业统一社会信用代码
    private String social_credit_code;
}

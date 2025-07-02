package com.aeotrade.provider.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2023-07-12 9:56
 */
@Data
public class wxUser implements Serializable {

    private String appid;

    private String headimgurl;
//
    private String nickname;

    private String openid;

    private String unionid;

    private String qrSceneStr;

    private String remark;

    private String sex;

    private Long subscribe_time;

    private String phone;
}

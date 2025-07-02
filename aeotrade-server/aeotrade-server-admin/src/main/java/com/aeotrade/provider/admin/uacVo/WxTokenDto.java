package com.aeotrade.provider.admin.uacVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WxTokenDto {
    private Long id ;
    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;
    private String nickname;
    private Integer sex;
    private String language;
    private String city;
    private String province;
    private String country;
    private String headimgurl;
    private String[] privilege;

}

package com.aeotrade.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WxTokenDto {

    private String access_token;
    private Long expires_in;
    private String refresh_token;
    private String openid;
    private String scope;
    private String unionid;

}

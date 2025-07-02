package com.aeotrade.provider.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-19 13:22
 */
@Data
public class GetToken {
    private String access_token;
    private String token_type;
    private String expires_in;
    private String scope;
    private String error;
}

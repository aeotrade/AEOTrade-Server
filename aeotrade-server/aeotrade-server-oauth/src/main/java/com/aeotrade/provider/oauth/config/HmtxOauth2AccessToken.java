package com.aeotrade.provider.oauth.config;


import lombok.Data;

import java.util.List;

/**
 * token 封装
 */
@Data
public class HmtxOauth2AccessToken {
    private long expiration;
    private boolean expired;
    private long expiresIn;
    private List<String> scope;
    private String tokenType;
    private String value;

}

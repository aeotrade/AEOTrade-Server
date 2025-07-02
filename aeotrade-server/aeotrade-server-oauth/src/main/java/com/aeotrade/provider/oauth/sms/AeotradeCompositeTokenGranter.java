package com.aeotrade.provider.oauth.sms;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.TokenRequest;

import java.util.ArrayList;
import java.util.List;


public class AeotradeCompositeTokenGranter implements TokenGranter {

    // TokenGranter 列表
    private  List<TokenGranter> tokenGranters;
//    @Autowired
//    private RedisService redisService;

    public AeotradeCompositeTokenGranter(List<TokenGranter> tokenGranters) {
        this.tokenGranters = new ArrayList<TokenGranter>(tokenGranters);
    }

    public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
        for (TokenGranter granter : tokenGranters) {
            OAuth2AccessToken grant = granter.grant(grantType, tokenRequest);
            if (grant!=null) {
                return grant;
            }
        }
        return null;
    }

    public void addTokenGranter(TokenGranter tokenGranter) {
        if (tokenGranter == null) {
            throw new IllegalArgumentException("Token granter is null");
        }
        tokenGranters.add(tokenGranter);
    }

}

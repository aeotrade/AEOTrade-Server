package com.aeotrade.provider.oauth.service;

import com.aeotrade.provider.util.mode.AdminAuthUser;
import com.aeotrade.provider.util.mode.AeotradeAuthUser;
import com.aeotrade.utlis.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class TokenJwtEnhancer implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication oAuth2Authentication) {
        Map<String, Object> info = new HashMap<>();
        info.put("timestamp", System.currentTimeMillis());
        Authentication authentication = oAuth2Authentication.getUserAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AeotradeAuthUser) {
            try {
                Map map = JacksonUtil.parseJson(JacksonUtil.toJson(authentication.getPrincipal()), Map.class);
                info.putAll(map);
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }else if(authentication != null && authentication.getPrincipal() instanceof AdminAuthUser){
            try {
                Map map = JacksonUtil.parseJson(JacksonUtil.toJson(authentication.getPrincipal()), Map.class);
                info.putAll(map);
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }

//        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

        return accessToken;

    }

}

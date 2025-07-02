package com.aeotrade.provider.oauth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.stereotype.Service;

/**
 * 重写UserInfoTokenServices
 * {@link UserInfoTokenServices#loadAuthentication(String)}
 *
 * @author hmm
 */
@Slf4j
@Service
public class HmtxUserInfoTokenServices implements ResourceServerTokenServices {
    private final TokenStore tokenStore;

        public HmtxUserInfoTokenServices(JwtAccessTokenConverter accessTokenConverter) {
            this.tokenStore = new JwtTokenStore(accessTokenConverter);
        }

        /**
         * Description: 用于从 accessToken 中加载凭证信息, 并构建出 {@link OAuth2Authentication} 的方法<br>
         *     Details:
         *
         * @see ResourceServerTokenServices#loadAuthentication(String)
         */
        @Override
        public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
            log.debug("HmtxUserInfoTokenServices :: loadAuthentication called ...");
            log.trace("HmtxUserInfoTokenServices :: loadAuthentication :: accessToken: {}", accessToken);
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(accessToken);
            if(oAuth2AccessToken.getExpiration().getTime()<System.currentTimeMillis()){
                throw  new InvalidTokenException("token过期");
            }else{
                OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(accessToken);
                return oAuth2Authentication;
            }
        }
        @Override
        public OAuth2AccessToken readAccessToken(String accessToken) {
            log.debug("CustomResourceServerTokenServices :: readAccessToken called ...");
            throw new UnsupportedOperationException("暂不支持 readAccessToken!");
        }
}
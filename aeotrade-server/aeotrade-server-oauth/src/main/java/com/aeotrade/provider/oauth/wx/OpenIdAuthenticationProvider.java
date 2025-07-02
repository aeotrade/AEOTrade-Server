package com.aeotrade.provider.oauth.wx;

import com.aeotrade.provider.oauth.service.WxUserDetailsService;
import lombok.SneakyThrows;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * @Author: yewei
 * @Date: 17:08 2020/11/25
 * @Description:
 */
public class OpenIdAuthenticationProvider implements AuthenticationProvider {

    private WxUserDetailsService userDetailsService;

    public OpenIdAuthenticationProvider( WxUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) {
        OpenIdAuthenticationToken authenticationToken = (OpenIdAuthenticationToken) authentication;
        String openId = (String) authenticationToken.getPrincipal();
        UserDetails user = userDetailsService.loadUserByOpenId(openId);
        if (user == null) {
            throw new InternalAuthenticationServiceException("openId错误");
        }
        OpenIdAuthenticationToken authenticationResult = new OpenIdAuthenticationToken(user, user.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OpenIdAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public WxUserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(WxUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}

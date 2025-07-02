package com.aeotrade.provider.oauth.single;

import com.aeotrade.provider.oauth.service.SingleUserDetailsService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;

/**
 * @Auther: 吴浩
 * @Date: 2021-07-01 14:39
 */
public class SingleAuthenticationProvider implements AuthenticationProvider {
    /**
     * UserDetailsService
     */
    private SingleUserDetailsService UserDetailsService;



    public SingleAuthenticationProvider(SingleUserDetailsService UserDetailsService) {
        this.UserDetailsService = UserDetailsService;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        SingleAuthenticationToken singleAuthenticationToken = (SingleAuthenticationToken) authentication;

        Object resObj;
        String res = Objects.nonNull(resObj = singleAuthenticationToken.getPrincipal())
                ? resObj.toString() : StringUtils.EMPTY;
        // 验证用户
        if (StringUtils.isBlank(res)) {
            throw new InternalAuthenticationServiceException("北京单一窗口用户唯一标识为空!");
        }
        // 根据电话号码获取用户
        UserDetails userDetails = UserDetailsService.loadUserByMoblie(res);
        if (Objects.isNull(userDetails)) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService 为空");
        }
        // 封装需要认证的PhoneAndVerificationCodeAuthenticationToken对象
        return new SingleAuthenticationToken(userDetails.getAuthorities(),userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SingleAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

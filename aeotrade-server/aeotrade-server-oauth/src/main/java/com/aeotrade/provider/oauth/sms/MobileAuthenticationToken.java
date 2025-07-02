package com.aeotrade.provider.oauth.sms;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @Author: yewei
 * @Date: 9:06 2020/11/25
 * @Description:
 * 在OAuth2认证开始认证时，
 * 会提前Authentication认证信息，
 * 然后交由AuthenticationManager认证。
 * 定义电话号码+验证码的Authentication认证信息
 */
public class MobileAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 手机号
     */
    private final Object mobile;

    /**
     * 验证码
     */
    private final Object code;



    public MobileAuthenticationToken(Object mobile, Object code) {
        super(null);
        this.mobile = mobile;
        this.code = code;
    }

    public MobileAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object mobile, Object code) {
        super(authorities);
        this.mobile = mobile;
        this.code = code;
        // 认证已经通过
        setAuthenticated(true);
    }

    /**
     * 用户身份凭证（一般是密码或者验证码）
     */
    @Override
    public Object getCredentials() {
        return code;
    }

    /**
     * 身份标识（一般是姓名，手机号）
     */
    @Override
    public Object getPrincipal() {
        return mobile;
    }
}

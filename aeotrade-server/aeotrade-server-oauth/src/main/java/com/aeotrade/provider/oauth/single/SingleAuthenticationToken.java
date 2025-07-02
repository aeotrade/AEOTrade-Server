package com.aeotrade.provider.oauth.single;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * @Auther: 吴浩
 * @Date: 2021-07-01 14:35
 */
public class SingleAuthenticationToken extends AbstractAuthenticationToken {
    /**
     * 北京单一窗口用户唯一标识
     */
    private final Object res;


    public SingleAuthenticationToken(Object res) {
        super(null);
        this.res = res;
    }

    public SingleAuthenticationToken(Collection<? extends GrantedAuthority> authorities, Object res) {
        super(authorities);
        this.res = res;
        // 认证已经通过
        setAuthenticated(true);
    }
    /**
     * 用户身份凭证（一般是密码或者验证码）
     */
    @Override
    public Object getCredentials() {
        return null;
    }

    /**
     * 身份标识（一般是姓名，手机号）
     */
    @Override
    public Object getPrincipal() {
        return res;
    }
}

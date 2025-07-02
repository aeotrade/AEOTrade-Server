package com.aeotrade.provider.oauth.refreshStaffToken;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class RefreshStaffToken extends AbstractAuthenticationToken {
    private final Object staffId;
    private final Object principal;
    public RefreshStaffToken(Object staffId, Object principal) {
        super(null);
        this.staffId = staffId;
        this.principal = principal;
    }

    public RefreshStaffToken(Collection<? extends GrantedAuthority> authorities, Object staffId, Object principal) {
        super(authorities);
        this.staffId = staffId;
        this.principal = principal;
        // 认证已经通过
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public Object getStaffId() {
        return staffId;
    }
}

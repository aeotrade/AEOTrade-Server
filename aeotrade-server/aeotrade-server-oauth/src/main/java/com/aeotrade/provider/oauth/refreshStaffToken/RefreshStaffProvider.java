package com.aeotrade.provider.oauth.refreshStaffToken;

import com.aeotrade.provider.oauth.service.MoblieUserDetailsService;
import com.aeotrade.provider.oauth.sms.MobileAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;

public class RefreshStaffProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;

    public RefreshStaffProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RefreshStaffToken refreshStaffToken = (RefreshStaffToken) authentication;
        UserDetails userDetails = userDetailsService.loadUserByUsername(refreshStaffToken.getStaffId().toString());
        if (Objects.isNull(userDetails)) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService 为空");
        }

        return new RefreshStaffToken(userDetails.getAuthorities(), refreshStaffToken.getStaffId(), userDetails);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RefreshStaffToken.class.isAssignableFrom(authentication);
    }
}

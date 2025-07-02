package com.aeotrade.provider.oauth.refreshStaffToken;

import com.aeotrade.provider.oauth.sms.MobileAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

public class RefreshStaffTokenGranter extends AbstractTokenGranter {
    private static final String GRANT_TYPE = "refresh_staff_token";

    private final AuthenticationManager authenticationManager;
    private ClientDetailsService clientDetailsService;

    public RefreshStaffTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                              ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory) {
        this(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.clientDetailsService = clientDetailsService;
    }
    protected RefreshStaffTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices,
                                 ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.authenticationManager = authenticationManager;
    }

    @Override
    public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
        if (!GRANT_TYPE.equals(grantType)) {
            return null;
        }

        String clientId = tokenRequest.getClientId();
        ClientDetails client = this.clientDetailsService.loadClientByClientId(clientId);
//        validateGrantType(grantType, client);

        if (logger.isDebugEnabled()) {
            logger.debug("Getting access token for: " + clientId);
        }

        return getAccessToken(client, tokenRequest);
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        // 电话号码与验证码
        String staffId = parameters.get("staffId");

        Authentication userAuth = new RefreshStaffToken(staffId,null);
        ((AbstractAuthenticationToken) userAuth).setDetails(parameters);
        try {
            // authenticationManager进行验证
            userAuth = authenticationManager.authenticate(userAuth);
        } catch (AccountStatusException ase) {
            //covers expired, locked, disabled cases (mentioned in section 5.2, draft 31)
            throw new InvalidGrantException(ase.getMessage());
        } catch (BadCredentialsException e) {
            // If the username/password are wrong the spec says we should send 400/invalid grant
            throw new InvalidGrantException(e.getMessage());
        }
        if (userAuth == null || !userAuth.isAuthenticated()) {
            throw new InvalidGrantException("Could not authenticate staffId : " + staffId);
        }
        OAuth2Request storedOAuth2Request = getRequestFactory().createOAuth2Request(client, tokenRequest);
        return new OAuth2Authentication(storedOAuth2Request, userAuth);
    }
}

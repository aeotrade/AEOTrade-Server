package com.aeotrade.provider.oauth.authorizationCode;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

import java.util.*;

public class AeotradeAccessTokenConverter implements AccessTokenConverter {
    private UserAuthenticationConverter userTokenConverter = new DefaultUserAuthenticationConverter();

    private boolean includeGrantType;

    private String scopeAttribute = SCOPE;

    private String clientIdAttribute = CLIENT_ID;
    private String key;

    public AeotradeAccessTokenConverter() {
    }

    public AeotradeAccessTokenConverter(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Converter for the part of the data in the token representing a user.
     *
     * @param userTokenConverter the userTokenConverter to set
     */
    public void setUserTokenConverter(UserAuthenticationConverter userTokenConverter) {
        this.userTokenConverter = userTokenConverter;
    }

    /**
     * Flag to indicate the the grant type should be included in the converted token.
     *
     * @param includeGrantType the flag value (default false)
     */
    public void setIncludeGrantType(boolean includeGrantType) {
        this.includeGrantType = includeGrantType;
    }

    /**
     * Set scope attribute name to be used in the converted token. Defaults to
     * {@link AccessTokenConverter#SCOPE}.
     *
     * @param scopeAttribute the scope attribute name to use
     */
    public void setScopeAttribute(String scopeAttribute) {
        this.scopeAttribute = scopeAttribute;
    }

    /**
     * Set client id attribute name to be used in the converted token. Defaults to
     * {@link AccessTokenConverter#CLIENT_ID}.
     *
     * @param clientIdAttribute the client id attribute name to use
     */
    public void setClientIdAttribute(String clientIdAttribute) {
        this.clientIdAttribute = clientIdAttribute;
    }

    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        Map<String, Object> response = new HashMap<String, Object>();
        OAuth2Request clientToken = authentication.getOAuth2Request();

        if (!authentication.isClientOnly()) {
            response.putAll(userTokenConverter.convertUserAuthentication(authentication.getUserAuthentication()));
        } else {
            if (clientToken.getAuthorities()!=null && !clientToken.getAuthorities().isEmpty()) {
                response.put(UserAuthenticationConverter.AUTHORITIES,
                        AuthorityUtils.authorityListToSet(clientToken.getAuthorities()));
            }
        }

        if (token.getScope()!=null) {
            response.put(scopeAttribute, token.getScope());
        }
        if (token.getAdditionalInformation().containsKey(JTI)) {
            response.put(JTI, token.getAdditionalInformation().get(JTI));
        }

        if (token.getExpiration() != null) {
            response.put(EXP, token.getExpiration().getTime() / 1000);
        }

        if (includeGrantType && authentication.getOAuth2Request().getGrantType()!=null) {
            response.put(GRANT_TYPE, authentication.getOAuth2Request().getGrantType());
        }

        response.putAll(token.getAdditionalInformation());

        response.put(clientIdAttribute, clientToken.getClientId());
        if (clientToken.getResourceIds() != null && !clientToken.getResourceIds().isEmpty()) {
            response.put(AUD, clientToken.getResourceIds());
        }
        response.put("key",getKey());
        return response;
    }

    public OAuth2AccessToken extractAccessToken(String value, Map<String, ?> map) {
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(value);
        Map<String, Object> info = new HashMap<String, Object>(map);
        info.remove(EXP);
        info.remove(AUD);
        info.remove(clientIdAttribute);
        info.remove(scopeAttribute);
        if (map.containsKey(EXP)) {
            token.setExpiration(new Date((Long) map.get(EXP) * 1000L));
        }
        if (map.containsKey(JTI)) {
            info.put(JTI, map.get(JTI));
        }
        token.setScope(extractScope(map));
        token.setAdditionalInformation(info);
        return token;
    }

    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        Map<String, String> parameters = new HashMap<String, String>();
        Set<String> scope = extractScope(map);
        Authentication user = userTokenConverter.extractAuthentication(map);
        String clientId = (String) map.get(clientIdAttribute);
        parameters.put(clientIdAttribute, clientId);
        if (includeGrantType && map.containsKey(GRANT_TYPE)) {
            parameters.put(GRANT_TYPE, (String) map.get(GRANT_TYPE));
        }
        Set<String> resourceIds = new LinkedHashSet<String>(map.containsKey(AUD) ? getAudience(map)
                : Collections.<String>emptySet());

        Collection<? extends GrantedAuthority> authorities = null;
        if (user==null && map.containsKey(AUTHORITIES)) {
            @SuppressWarnings("unchecked")
            String[] roles = ((Collection<String>)map.get(AUTHORITIES)).toArray(new String[0]);
            authorities = AuthorityUtils.createAuthorityList(roles);
        }
        OAuth2Request request = new OAuth2Request(parameters, clientId, authorities, true, scope, resourceIds, null, null,
                null);
        return new OAuth2Authentication(request, user);
    }

    private Collection<String> getAudience(Map<String, ?> map) {
        Object auds = map.get(AUD);
        if (auds instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> result = (Collection<String>) auds;
            return result;
        }
        return Collections.singleton((String)auds);
    }

    private Set<String> extractScope(Map<String, ?> map) {
        Set<String> scope = Collections.emptySet();
        if (map.containsKey(scopeAttribute)) {
            Object scopeObj = map.get(scopeAttribute);
            if (String.class.isInstance(scopeObj)) {
                scope = new LinkedHashSet<String>(Arrays.asList(String.class.cast(scopeObj).split(" ")));
            } else if (Collection.class.isAssignableFrom(scopeObj.getClass())) {
                @SuppressWarnings("unchecked")
                Collection<String> scopeColl = (Collection<String>) scopeObj;
                scope = new LinkedHashSet<String>(scopeColl);	// Preserve ordering
            }
        }
        return scope;
    }
}

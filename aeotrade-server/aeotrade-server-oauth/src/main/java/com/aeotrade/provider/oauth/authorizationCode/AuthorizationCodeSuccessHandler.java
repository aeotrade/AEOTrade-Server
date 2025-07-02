package com.aeotrade.provider.oauth.authorizationCode;

import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUserConnection;
import com.aeotrade.provider.service.impl.SmweixinService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.async.UserConnectionService;
import com.aeotrade.provider.util.mode.AeotradeAuthUser;
import com.aeotrade.suppot.RespResultMapper;
import com.aeotrade.utlis.JacksonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthorizationCodeSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserConnectionService userConnectionService;
    private final UacStaffService uacStaffService;
    private final SmweixinService smweixinService;
    private final DefaultTokenServices tokenService;
    private final ClientDetailsService clientDetailsService;

    private final String redirectOauth2GatewayUrl;

    private RequestCache requestCache = new HttpSessionRequestCache();

    public AuthorizationCodeSuccessHandler(UserConnectionService userConnectionService,
                                           UacStaffService uacStaffService,
                                           SmweixinService smweixinService,
                                           DefaultTokenServices tokenService,
                                           ClientDetailsService clientDetailsService,
                                           String redirectOauth2GatewayUrl) {
        super();
        this.userConnectionService = userConnectionService;
        this.uacStaffService = uacStaffService;
        this.smweixinService = smweixinService;
        this.tokenService = tokenService;
        this.clientDetailsService = clientDetailsService;
        this.redirectOauth2GatewayUrl = redirectOauth2GatewayUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        //如果不是授权码模式登录，则执行onThisAuthenticationSuccess动作
        if (savedRequest == null) {
            onThisAuthenticationSuccess(request, response, authentication);

            return;
        }
        String targetUrlParameter = getTargetUrlParameter();
        if (isAlwaysUseDefaultTargetUrl()
                || (targetUrlParameter != null && StringUtils.hasText(request
                .getParameter(targetUrlParameter)))) {
            requestCache.removeRequest(request, response);
            super.onAuthenticationSuccess(request, response, authentication);

            return;
        }

        clearAuthenticationAttributes(request);

        // Use the DefaultSavedRequest URL
        String targetUrl = null;
        if (StringUtils.isEmpty(redirectOauth2GatewayUrl)){
            targetUrl = savedRequest.getRedirectUrl();
        }else {
            targetUrl = redirectOauth2GatewayUrl+((DefaultSavedRequest) savedRequest).getServletPath()+"?"+((DefaultSavedRequest) savedRequest).getQueryString();
        }

        logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);

//        response.setHeader("Access-Control-Allow-Credentials", "True");
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Max-Age", "3600");
//        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
//        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }
    public void onThisAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        ClientDetails clientDetails=clientDetailsService.loadClientByClientId("wechat");

        TokenRequest tokenRequest = new TokenRequest(new HashMap<>(), clientDetails.getClientId(), clientDetails.getScope(), clientDetails.getAuthorizedGrantTypes().toString());

        // 通过 TokenRequest的 createOAuth2Request方法获取 OAuth2Request
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);
        // 通过 Authentication和 OAuth2Request构造出 OAuth2Authentication
        OAuth2Authentication auth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
        // 通过 AuthorizationServerTokenServices 生成 OAuth2AccessToken
        OAuth2AccessToken token = tokenService.createAccessToken(auth2Authentication);

        AeotradeAuthUser aeotradeAuthUser= (AeotradeAuthUser) authentication.getPrincipal();
        String string = new ObjectMapper().writeValueAsString(RespResultMapper.ok(login(aeotradeAuthUser,token)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (request.getHeader(HttpHeaders.ORIGIN)!=null) {
            response.setHeader(HttpHeaders.SET_COOKIE, "JSESSIONID=" + request.getSession().getId() + ";domain=" + (request.getHeader(HttpHeaders.ORIGIN).startsWith("http") ? request.getHeader(HttpHeaders.ORIGIN).substring(request.getHeader(HttpHeaders.ORIGIN).indexOf(".")) : request.getHeader(HttpHeaders.ORIGIN)) + "; Path=/; HttpOnly");
        }else  {
            response.setHeader(HttpHeaders.SET_COOKIE, "JSESSIONID=" + request.getSession().getId() + ";domain=" +
                    (request.getHeader(HttpHeaders.HOST).indexOf(":")>0
                            ? request.getHeader(HttpHeaders.HOST).substring(0,request.getHeader(HttpHeaders.HOST).indexOf(":"))
                            : (request.getHeader(HttpHeaders.HOST).indexOf(".")>0
                            ? request.getHeader(HttpHeaders.HOST).substring(request.getHeader(HttpHeaders.HOST).indexOf("."))
                            : request.getHeader(HttpHeaders.HOST)))+ "; Path=/; HttpOnly");
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(string);
    }

    public Map<String, Object> login(AeotradeAuthUser aeotradeAuthUser,OAuth2AccessToken token) throws IOException {
        Map<String, Object> map = JacksonUtil.parseJson(JacksonUtil.toJsonWithFormat(token), Map.class);
        UacUserConnection uacUser = userConnectionService.findUacUserConnectionBy(aeotradeAuthUser.getStaffId());
        UacStaff uacStaff = uacStaffService.getById(aeotradeAuthUser.getStaffId());
        //记录缓存，用于网关适配长短token逻辑
        userConnectionService.redisOauth2Token(aeotradeAuthUser.getStaffId().toString(),"web",token);
        return smweixinService.loginjson(map, uacUser, uacStaff);
    }
    public void setRequestCache(RequestCache requestCache) {
        this.requestCache = requestCache;
    }
}

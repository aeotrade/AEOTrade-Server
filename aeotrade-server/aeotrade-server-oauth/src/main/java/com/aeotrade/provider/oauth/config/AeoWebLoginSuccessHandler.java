package com.aeotrade.provider.oauth.config;

import com.aeotrade.utlis.AeoResponse;
import com.aeotrade.utlis.AeoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
@Slf4j
@Component
public class AeoWebLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {

        SavedRequest savedRequest = requestCache.getRequest(request, response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object attribute = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
            log.info("跳转到登录页的地址为: {}", attribute);
        }
        if (AeoUtil.isAjaxRequest(request)) {
            AeoResponse data = new AeoResponse();
            if (savedRequest == null) {
                AeoUtil.makeFailureResponse(response, data.message("请通过授权码模式跳转到该页面"));
                return;
            }
            data.result(savedRequest.getRedirectUrl());
            AeoUtil.makeSuccessResponse(response, data);
        } else {
            if (savedRequest == null) {
                super.onAuthenticationSuccess(request, response, authentication);
                return;
            }
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, savedRequest.getRedirectUrl());
        }
    }

}

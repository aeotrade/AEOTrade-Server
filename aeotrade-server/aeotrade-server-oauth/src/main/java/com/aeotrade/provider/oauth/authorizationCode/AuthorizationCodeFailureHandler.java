package com.aeotrade.provider.oauth.authorizationCode;

import com.aeotrade.provider.oauth.sms.MobileCodeException;
import com.aeotrade.suppot.RespResultMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationCodeFailureHandler implements AuthenticationFailureHandler {
    public AuthorizationCodeFailureHandler() {
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String message;
        if (exception instanceof BadCodeException){
            message = new ObjectMapper().writeValueAsString(RespResultMapper.error(exception.getMessage()));
        } else if (exception instanceof MobileCodeException) {
            message = new ObjectMapper().writeValueAsString(RespResultMapper.error(exception.getMessage()));
        } else {
            message = new ObjectMapper().writeValueAsString(RespResultMapper.error("用户名或密码错误"));
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(message);
    }
}

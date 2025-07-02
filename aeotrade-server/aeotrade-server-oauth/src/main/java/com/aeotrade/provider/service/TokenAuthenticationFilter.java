package com.aeotrade.provider.service;

import com.aeotrade.provider.oauth.service.HmtxUserInfoTokenServices;
import com.aeotrade.provider.service.async.UserConnectionService;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Future;

@Component
@Slf4j
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;
    @Autowired
    private UserConnectionService userConnectionService;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse
            httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        String token = "";
        String StaffId = "";
        String memberId = "";
        //获取请求地址和请求参数;
        String parameter = httpServletRequest.getParameter("client_id");
        Map<String, String[]> map = httpServletRequest.getParameterMap();
        String servletPath = httpServletRequest.getServletPath();
        //判断请求地址是否等于/oauth/authorize
        if (servletPath.equals("/oauth/authorize")) {
            //取出Cookie值
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies!=null&&cookies.length != 0) {
                for (Cookie cookie : cookies) {
                    //取出用户Id
                    if (cookie.getName().equals("staffId")) {
                        StaffId = cookie.getValue();
                    }
                    //取出用户Id
                    if (cookie.getName().equals("memberId")) {
                        memberId = cookie.getValue();
                    }
                }
            }
            if (StringUtils.isNotBlank(memberId) && StringUtils.isNotBlank(StaffId)) {
                    try {
                        Field locked = map.getClass().getDeclaredField("locked");
                        locked.setAccessible(true);//打开访问权限，让他裸奔，private类型照样玩他
                        locked.setBoolean(map, false);//将lock参数设置为false了，就是可以修改了
                        map.put("redirect_uri", new String[]{httpServletRequest.getParameter("redirect_uri")});
                        map.put("response_type", new String[]{httpServletRequest.getParameter("response_type")});
                        map.put("scope", new String[]{httpServletRequest.getParameter("scope")});
                        log.info(JSON.toJSONString(map)+"++++++++++++++++++++++++++++++++++++++++++++++");
                        locked.setBoolean(map, true);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                    Future<OAuth2AccessToken> oAuth2AccessTokenFuture = userConnectionService.findMobile(StaffId,"web");
                    try {
                        OAuth2AccessToken oAuth2AccessToken = oAuth2AccessTokenFuture.get();
                        token = String.valueOf(oAuth2AccessToken);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                    if (StringUtils.isNotBlank(token)) {
                        //1.解析token
                        OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(token);
                        //2.新建并填充authentication
                        UsernamePasswordAuthenticationToken authentication = new
                                UsernamePasswordAuthenticationToken(
                                oAuth2Authentication.getPrincipal(), null, oAuth2Authentication.getAuthorities());
                        authentication.setDetails(oAuth2Authentication.getDetails());
                        //3.将authentication保存进安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }

            }
        }
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}

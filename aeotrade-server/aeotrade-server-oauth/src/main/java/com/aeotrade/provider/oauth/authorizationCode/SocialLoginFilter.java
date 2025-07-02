package com.aeotrade.provider.oauth.authorizationCode;

import com.aeotrade.base.constant.AeoConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 重写过滤器的attemptAuthentication方法
 * 主要就是用户信息的获取从开始的表单获取改成json数据获取
 */
@Slf4j
public class SocialLoginFilter extends UsernamePasswordAuthenticationFilter {
    public RedisTemplate<String, String> getRedisTemplate() {
        return redisTemplate;
    }

    public void setRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //判断是否是post请求
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }
        //判断是否是json参数,提取json参数里面的用户信息
        if(request.getContentType().equals("application/json")){
            try {
                //参数转换为map
                Map map = new ObjectMapper().readValue(request.getInputStream(),Map.class);

                //验证码
                String requestId=request.getHeader("U-ID");
                String code = (String) map.get("vcode");
                String vmessage = validateCode(code, requestId);
                if (StringUtils.isNotBlank(vmessage)) {
                    throw new BadCodeException("验证码错误");
                }

                String username = (String) map.get(super.getUsernameParameter());
                String password = (String) map.get(super.getPasswordParameter());
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, authRequest);
                return super.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }
        return super.attemptAuthentication(request, response);
    }
    private String validateCode(String code, String requestId) {
        // 如果vcode为空，跳过验证码验证,主要是适配之前的业务
        if (StringUtils.isBlank(code)) {
            return "验证码不能为空";
        }
        // 如果有参数vcode,但没有U-ID，进行提醒，这一般是调接口时发生的错误
        if (StringUtils.isNotBlank(code) && StringUtils.isBlank(requestId)) {
            return "缺少参数，U-ID";
        }
        // 符合参数验证条件
        String s = redisTemplate.opsForValue().get(AeoConstant.IMAGEREDIS_KEY + "_" + requestId);
        if (StringUtils.isBlank(s)) {
            return "验证码已失效";
        }
        if (!StringUtils.equalsIgnoreCase(s, code)) {
            return "验证码错误";
        }
        return "";
    }
}

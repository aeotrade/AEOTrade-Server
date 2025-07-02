package com.aeotrade.provider.oauth.sms;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.provider.oauth.service.MoblieUserDetailsService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;

/**
 * @Author: yewei
 * @Date: 9:08 2020/11/25
 * @Description:
 * 在AuthenticationManager认证过程中，
 * 是通过AuthenticationProvider接口的扩展来实现自定义认证方式的。
 * 定义手机和验证码认证提供者PhoneAndVerificationCodeAuthenticationProvider
 */
@Slf4j
public class MobileAuthenticationProvider implements AuthenticationProvider {


    /**
     * UserDetailsService
     */
    private MoblieUserDetailsService UserDetailsService;

    /**
     * redis服务
     */
    private StringRedisTemplate stringRedisTemplate;


    public MobileAuthenticationProvider(MoblieUserDetailsService UserDetailsService, StringRedisTemplate redisTemplate) {
        this.UserDetailsService = UserDetailsService;
        this.stringRedisTemplate = redisTemplate;
    }

    @SneakyThrows
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        MobileAuthenticationToken phoneAndVerificationCodeAuthenticationToken = (MobileAuthenticationToken) authentication;

        Object verificationCodeObj;
        String verificationCode = Objects.nonNull(verificationCodeObj = phoneAndVerificationCodeAuthenticationToken.getCredentials()) ?
                verificationCodeObj.toString() : StringUtils.EMPTY;

        Object phoneNumberObj;
        String phoneNumber = Objects.nonNull(phoneNumberObj = phoneAndVerificationCodeAuthenticationToken.getPrincipal())
                ? phoneNumberObj.toString() : StringUtils.EMPTY;
        // 验证用户
        if (StringUtils.isBlank(phoneNumber)) {
            throw new InternalAuthenticationServiceException("电话号码为空!");
        }
        // 根据电话号码获取用户
        UserDetails userDetails = UserDetailsService.loadUserByMoblie(phoneNumber);
        if (Objects.isNull(userDetails)) {
            throw new InternalAuthenticationServiceException(
                    "UserDetailsService 为空");
        }
        //校验验证码
        //验证的过期时间
        Long expire = stringRedisTemplate.opsForValue().getOperations().getExpire(AeoConstant.SMSREDIS_KEY+phoneNumber);
        log.info("redis过期时间返回"+expire);
        if (  expire <= 0 ) {
            throw new InternalAuthenticationServiceException(
                    "验证码已过期");
        }
        String cmsCode = this.getCmsCode(phoneNumber);
        if ( cmsCode == null && StringUtils.isEmpty(cmsCode)) {
            throw new InternalAuthenticationServiceException(
                    "验证码为空!");
        }
        //校验验证码
        if (!verificationCode.equals(cmsCode)){
            throw new InternalAuthenticationServiceException(
                    "验证码错误!");
        }
        stringRedisTemplate.delete(AeoConstant.SMSREDIS_KEY+phoneNumber);
        // 封装需要认证的PhoneAndVerificationCodeAuthenticationToken对象
        return new MobileAuthenticationToken(userDetails.getAuthorities(), userDetails, verificationCode);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MobileAuthenticationToken.class.isAssignableFrom(authentication);
    }
    //从redis查询验证码
    public String getCmsCode(String phoneNumber) {
        //从redis中取到令牌信息
        String value = stringRedisTemplate.opsForValue().get(AeoConstant.SMSREDIS_KEY+phoneNumber);
        //转成对象
        return StringUtils.isEmpty(value)?"":value;
    }
}


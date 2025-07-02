package com.aeotrade.provider.oauth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

/**
 * @Author: yewei
 * @Date: 17:08 2020/11/25
 * @Description:
 */
public interface WxUserDetailsService {
    UserDetails loadUserByOpenId(String openId) throws IOException;
}

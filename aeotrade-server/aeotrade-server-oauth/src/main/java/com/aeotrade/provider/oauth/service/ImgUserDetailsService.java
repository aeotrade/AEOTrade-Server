package com.aeotrade.provider.oauth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

/**
 * @Author: yewei
 * @Date: 19:01 2020/11/23
 * @Description:
 */


public interface ImgUserDetailsService {
    UserDetails loadUserByMoblie(String mobile) throws IOException;
}

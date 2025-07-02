package com.aeotrade.provider.oauth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

/**
 * @Auther: 吴浩
 * @Date: 2021-07-01 16:39
 */
public interface SingleUserDetailsService {
    UserDetails loadUserByMoblie(String res) throws IOException;
}

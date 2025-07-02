package com.aeotrade.provider.oauth;

import com.aeotrade.provider.util.SHA256Util;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String password = SHA256Util.randomPassword();
        System.out.println(password+"="+bCryptPasswordEncoder.encode(password));
        System.out.println(bCryptPasswordEncoder.matches("bJW7fy4srl","$2a$10$lJ1oqgsVqdq6ZJcqRLOzmOahdl5bp2zauANdC4ECMF5vsK34hjfI6"));
    }
}

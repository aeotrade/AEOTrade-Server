package com.aeotrade.provider.oauth.service;

/**
 * @Author: yewei
 * @Date: 15:17 2020/12/21
 * @Description:
 */
public interface IValidateCodeService {
    void validate(String username, String code);
}

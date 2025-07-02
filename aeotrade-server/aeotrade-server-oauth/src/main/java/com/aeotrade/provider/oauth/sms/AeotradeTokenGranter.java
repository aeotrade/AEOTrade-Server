package com.aeotrade.provider.oauth.sms;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.TokenRequest;

/**
 * 默认的只有授权码类型，简化类型，密码类型，客户端类型。这里需要新增一种电话号码+验证码的认证和生成访问授权码的TokenGranter。接口TokenGranter定义了token获取方法
 */

public interface AeotradeTokenGranter {
    OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest);
}

package com.aeotrade.provider.oauth.img;

import com.aeotrade.provider.oauth.service.IValidateCodeService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: yewei
 * @Date: 9:10 2020/11/25
 * @Description:
 * 新增电话验证码类型，PhoneAndVerificationCodeTokenGranter，
 * 参考密码类型ResourceOwnerPasswordTokenGranter的认证流程，
 * 首先进行电话号码与验证码的认证，然后生成访问授权码
 */
public class ImgTokenGranter extends ResourceOwnerPasswordTokenGranter {

    private static final String GRANT_TYPE = "password_code";

    private final IValidateCodeService validateCodeService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public ImgTokenGranter(AuthenticationManager authenticationManager, AuthorizationServerTokenServices tokenServices
            , ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, IValidateCodeService validateCodeService) {
        super(authenticationManager, tokenServices, clientDetailsService, requestFactory, GRANT_TYPE);
        this.validateCodeService = validateCodeService;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        /*String password_code = passwordEncoder.encode("password_code");
        log.info(password_code);*/
        Map<String, String> parameters = new LinkedHashMap<>(tokenRequest.getRequestParameters());
        String username = parameters.get("username");
        String code = parameters.get("code");
        //校验图形验证码
        validateCodeService.validate(username, code);
        String s = JSONObject.toJSONString(parameters);
        Map<String, String> map = JSONObject.parseObject(s, Map.class);
        String username1 = "code-" + parameters.get("username");
        map.put("username",username1);
        tokenRequest.setRequestParameters(map);
        return super.getOAuth2Authentication(client, tokenRequest);
    }
}
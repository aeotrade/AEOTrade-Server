package com.aeotrade.provider.oauth.service.impl;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.oauth.config.ValidateCodeException;
import com.aeotrade.provider.oauth.service.IValidateCodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
/**
 * @Author: yewei
 * @Date: 15:20 2020/12/21
 * @Description:
 */
@Slf4j
@Service
public class IValidateCodeServiceImpl implements IValidateCodeService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void validate(String username, String code) {

        if (StringUtils.isBlank(username)) {
            throw new ValidateCodeException("请在请求参数中携带username参数");
        }
        if (StringUtils.isBlank(code)) {
            throw new ValidateCodeException("请填写验证码");
        }
        //判断数据库存的验证码是否正确
        String code1 = redisTemplate.opsForValue().get(AeoConstant.IMAGEREDIS_KEY + username);
        log.info("code为:::::::;" + code1);
        if (code1 == null) {
            throw new ValidateCodeException("验证码不存在");
        }
        //验证的过期时间
        Long expire = redisTemplate.opsForValue().getOperations().getExpire(AeoConstant.IMAGEREDIS_KEY + username);
        if (expire <= 0) {
            throw new ValidateCodeException(
                    "验证码已过期");
        }
        if (!code1.equals(code.toUpperCase())) {
            throw new ValidateCodeException("验证码错误(" + code1 + "=" + code.toUpperCase()+")");
        }
        redisTemplate.delete(AeoConstant.IMAGEREDIS_KEY + username);
    }
}

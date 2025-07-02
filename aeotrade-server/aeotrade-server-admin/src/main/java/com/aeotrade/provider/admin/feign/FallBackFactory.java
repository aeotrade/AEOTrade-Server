package com.aeotrade.provider.admin.feign;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author: yewei
 * @Date: 10:34 2020/12/28
 * @Description:
 */
@Component
public class FallBackFactory implements FallbackFactory<OauthFeign> {
    @Override
    public OauthFeign create(Throwable cause) {
       return new OauthFeign() {
           @Override
           public Object getToken(Map<String, String> parameters) {
               return cause.getMessage();
           }
       };
    }
}

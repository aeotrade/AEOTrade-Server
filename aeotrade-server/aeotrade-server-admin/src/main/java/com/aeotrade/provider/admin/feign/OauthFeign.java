package com.aeotrade.provider.admin.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "aeotrade-service-oauth",path = "/",fallbackFactory = FallBackFactory.class)
public interface OauthFeign {

    @GetMapping (value = "/oauth/token")
    Object
    getToken(@RequestParam(value = "client_id") Map<String, String> parameters);

}

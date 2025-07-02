package com.aeotrade.provider.admin.feign;

import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author admin
 */
@FeignClient(name = "aeotrade-provider-mamber", path = "/",fallback = MamberFeignCallback.class)
public interface MamberFeign {
    @GetMapping(value = "/uaw/VipMessage/loginMessage")
    RespResult loginMessage(@RequestParam(value = "id") Long id, @RequestParam(value = "apply") int apply);
}

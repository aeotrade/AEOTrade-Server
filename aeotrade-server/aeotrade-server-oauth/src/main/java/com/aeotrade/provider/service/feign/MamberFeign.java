package com.aeotrade.provider.service.feign;

import com.aeotrade.provider.service.fallback.MamberFeignCallback;
import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "aeotrade-provider-mamber",path = "/",fallback = MamberFeignCallback.class)
public interface MamberFeign {

    @GetMapping(value = "uaw/VipMessage/loginMessage")
    RespResult loginMessage(@RequestParam(value = "id") Long id, @RequestParam(value = "apply") int apply);

    @GetMapping(value = "uaw/VipMessage/vip")
    RespResult openVip(@RequestParam(value = "id") Long id,
                       @RequestParam(value = "memberName") String memberName,
                       @RequestParam(value = "uscc") String uscc,
                       @RequestParam(value = "vipClassId") Long vipClassId,
                       @RequestParam(value = "vipTypeId") Long vipTypeId);
}

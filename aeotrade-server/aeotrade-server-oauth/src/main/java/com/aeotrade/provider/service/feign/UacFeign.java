package com.aeotrade.provider.service.feign;

import com.aeotrade.provider.service.fallback.UacFeignCallback;
import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "aeotrade-provider-uac",path = "/uac/manager/",fallbackFactory = UacFeignCallback.class)
public interface UacFeign {
    /**
     * 根据询价表id查询询价详情
     * @param staffId
     * @return
     */
    @GetMapping(value = "subAdmins")
    public RespResult subAdminList(@RequestParam(value = "staffId") Long staffId);
}

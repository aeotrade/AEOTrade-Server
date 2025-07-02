package com.aeotrade.provider.mamber.feign;

import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: yewei
 * @Date: 11:00 2021-12-31
 * @Description:
 */
@FeignClient(name = "aeotrade-provider-admin",path = "/",fallback = AdminFeignCallback.class)
public interface AdminFeign {

    @GetMapping(value = "role/model/role/member/all")
    String sendToken(@RequestParam("memberId")Long memberId,
                     @RequestParam("type") Integer type,
                     @RequestParam("platformId") Long platformId,
                     @RequestParam("orgn") String orgn);

    @GetMapping(value = "/uac/manager/ai/member")
    RespResult findMember(@RequestParam(value = "fieldNames") String fieldNames, @RequestParam(value = "id") String id,
                          @RequestParam(value = "sort") String sort, @RequestParam(value = "type") Integer type,
                          @RequestParam(value = "tableName") String tableName);
}

package com.aeotrade.provider.mamber.feign;

import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: yewei
 * @Date: 2020/6/17 11:47
 */
@FeignClient(name = "aeotrade-service-weixin",path = "/wxcats/",fallback = WeixinFeignCallback.class)
public interface WeixinFeign {
    @GetMapping(value = "ai/usd")
    RespResult findMember(@RequestParam(value = "fieldNames")String fieldNames, @RequestParam(value = "id")String id,
                          @RequestParam(value = "sort")String sort, @RequestParam(value = "type")Integer type,
                          @RequestParam(value = "tableName")String tableName, @RequestParam(value = "pageSize")Long pageSize,
                          @RequestParam(value = "pageNum")Long pageNum);
    @GetMapping(value = "ai/page/usd")
    RespResult findPageMember(@RequestParam(value = "fieldNames")String fieldNames, @RequestParam(value = "id")String id,
                              @RequestParam(value = "sort")String sort, @RequestParam(value = "type")Integer type,
                              @RequestParam(value = "tableName")String tableName, @RequestParam(value = "pageSize")Long pageSize,
                              @RequestParam(value = "pageNum")Long pageNum);
}

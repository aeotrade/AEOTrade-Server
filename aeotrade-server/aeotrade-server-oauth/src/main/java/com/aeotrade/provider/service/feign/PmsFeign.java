package com.aeotrade.provider.service.feign;

import com.aeotrade.provider.dto.PmsTopicDto;
import com.aeotrade.provider.service.fallback.PmsFeignCallback;
import com.aeotrade.suppot.RespResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "aeotrade-provider-pms",path = "topic",fallback = PmsFeignCallback.class)
public interface PmsFeign {

    /**
     * 根据询价表id查询询价详情
     * @param id
     * @return
     */
    @GetMapping(value = "deta")
    RespResult findPmsTopicById(@RequestParam(value = "id") Long id);

    //询盘修改
    @PostMapping("/")
    RespResult updateTopic(@RequestBody PmsTopicDto pmsTopicDto);

}

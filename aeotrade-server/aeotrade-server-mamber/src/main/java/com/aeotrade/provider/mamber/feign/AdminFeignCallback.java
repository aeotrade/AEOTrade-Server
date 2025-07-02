package com.aeotrade.provider.mamber.feign;

import com.aeotrade.suppot.RespResult;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 11:08 2021-12-31
 * @Description:
 */
@Component
public class AdminFeignCallback implements AdminFeign {
    @Override
    public String sendToken(Long memberId, Integer type, Long platformId, String orgn) {
        return "0";
    }

    @Override
    public RespResult findMember(String fieldNames, String id, String sort, Integer type, String tableName) {
        return null;
    }
}

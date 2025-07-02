package com.aeotrade.provider.mamber.feign;


import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import org.springframework.stereotype.Component;

/**
 * @Author: yewei
 * @Date: 2020/6/17 11:49
 */
@Component
public class WeixinFeignCallback extends BaseController implements WeixinFeign {
    @Override
    public RespResult findMember(String fieldNames, String id, String sort, Integer type, String tableName, Long pageSize, Long pageNum) {
        return handleFail("调用失败");
    }

    @Override
    public RespResult findPageMember(String fieldNames, String id, String sort, Integer type, String tableName, Long pageSize, Long pageNum) {
        return handleFail("调用失败");
    }
}

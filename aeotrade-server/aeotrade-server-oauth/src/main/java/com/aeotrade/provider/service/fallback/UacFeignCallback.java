package com.aeotrade.provider.service.fallback;

import com.aeotrade.provider.service.feign.UacFeign;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.netflix.client.ClientException;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import feign.RetryableException;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class UacFeignCallback extends BaseController implements FallbackFactory<UacFeign> {

    @Override
    public UacFeign create(Throwable throwable) {
        return new UacFeign() {
            @Override
            public RespResult subAdminList(Long staffId) {
                if (throwable.getCause() instanceof ClientException) {
                    return handleFail("服务正在启动中，5分钟后再试。。。");
                }else if(throwable.getCause() instanceof RetryableException){
                    return handleFail("服务未启动。。。");
                }else if (throwable instanceof HystrixTimeoutException){
                    return handleFail("服务正在启动中，5分钟后再试。。。");
                }
                return handleFail(throwable.getMessage()!=null?throwable.getMessage():"服务未启动。。。");
            }
        };
    }
}

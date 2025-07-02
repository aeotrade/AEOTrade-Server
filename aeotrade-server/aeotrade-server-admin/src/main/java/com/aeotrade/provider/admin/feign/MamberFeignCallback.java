package com.aeotrade.provider.admin.feign;

import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import org.springframework.stereotype.Component;

@Component
public class MamberFeignCallback extends BaseController implements MamberFeign {
    @Override
    public RespResult loginMessage(Long id, int apply) {
        return handleFail("调用失败");
    }
}

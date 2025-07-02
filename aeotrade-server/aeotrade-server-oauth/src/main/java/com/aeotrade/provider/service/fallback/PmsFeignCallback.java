package com.aeotrade.provider.service.fallback;

import com.aeotrade.provider.dto.PmsTopicDto;
import com.aeotrade.provider.service.feign.PmsFeign;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import org.springframework.stereotype.Component;

@Component
public class PmsFeignCallback extends BaseController implements PmsFeign {
    @Override
    public RespResult findPmsTopicById(Long id) {
        return handleFail("error");
    }

    @Override
    public RespResult updateTopic(PmsTopicDto pmsTopicDto) {
        return handleFail("error");
    }
}

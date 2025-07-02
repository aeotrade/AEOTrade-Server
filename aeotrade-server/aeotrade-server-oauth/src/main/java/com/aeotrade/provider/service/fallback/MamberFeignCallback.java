package com.aeotrade.provider.service.fallback;

import com.aeotrade.provider.service.feign.MamberFeign;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class MamberFeignCallback extends BaseController implements MamberFeign {
    @Value("${hmtx.login.gateway-url}")
    private String gatewayUrl;
    @Override
    public RespResult loginMessage(Long id, int apply) {
        log.error("Feign添加会员信息失败");
        try {
            log.info("创建会员信息---------------------------------------------------------");
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("id", id);
            getMap.put("apply", apply);
            String s = HttpRequestUtils.httpGet(gatewayUrl+"/mam/uaw/VipMessage/loginMessage", getMap, null);
            log.info("http调用结果" + s);
            RespResult respResult = JSONObject.parseObject(s, RespResult.class);
            if(respResult.getCode()==200){
                return respResult;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail("添加会员信息失败");
        }
        return handleFail("调用失败");
    }

    @Override
    public RespResult openVip(Long id, String memberName, String uscc, Long vipClassId, Long vipTypeId) {
        log.error("Feign添加会员信息失败");
        try {
            log.info("创建会员信息---------------------------------------------------------");
            Map<String, Object> getMap = new HashMap<>();
            getMap.put("id", id);
            getMap.put("memberName",memberName);
            getMap.put("uscc",uscc);
            getMap.put("vipClassId", vipClassId);
            getMap.put("vipTypeId", vipTypeId);
            String s = HttpRequestUtils.httpGet(gatewayUrl+"/mam/uaw/VipMessage/vip", getMap, null);
            log.info("http调用结果" + s);
            RespResult respResult = JSONObject.parseObject(s, RespResult.class);
            if(respResult.getCode()==200){
                return respResult;
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail("添加会员信息失败");
        }
        return handleFail("调用失败");
    }
}

package com.aeotrade.provider.event;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UawVipType;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UawVipTypeService;
import com.aeotrade.service.MqSend;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
@Async(AeoConstant.ASYNC_POOL)
@Slf4j
@Component
public class AddStaffEventListener implements ApplicationListener<AddStaffEvent> {
    @Value("${hmtx.chain.id:aeotradechain}")
    private String chainId;
    private final MqSend mqSend;
    private final UacStaffService uacStaffService;
    private final UawVipTypeService uawVipTypeService;

    public AddStaffEventListener(MqSend mqSend, UacStaffService uacStaffService, UawVipTypeService uawVipTypeService) {
        this.mqSend = mqSend;
        this.uacStaffService = uacStaffService;
        this.uawVipTypeService = uawVipTypeService;
    }

    /**
     * 企业添加员工成功后，为员工申请当前企业下的证书并在链上进行身份认证
     */
    @Override
    public void onApplicationEvent(AddStaffEvent event) {
        try {
            if (event.getUacStaff() == null|| event.getUacMember() == null){
                log.warn("企业或员工信息为空,");
                return;
            }
            String relevancyTypeId;
            UacStaff uacStaffVipType = uacStaffService.getById(event.getUacMember().getStaffId());
            List<UawVipType> uawVipTypeList = uawVipTypeService.lambdaQuery().eq(UawVipType::getWorkbench, uacStaffVipType.getLastWorkbenchId()).list();
            if (uawVipTypeList.isEmpty()) {
                relevancyTypeId="01"; //默认贸易企业
            }else {
                UawVipType uawVipType = uawVipTypeList.get(0);
                relevancyTypeId = uawVipType.getRelevancyTypeId();
            }

            HashMap<String, String> chain = new HashMap<>(9);
            chain.put("tenantId", event.getUacMember().getId().toString());
            if (event.getUacMember().getMemberName()==null){
                return;
            }
            chain.put("tenantName", event.getUacMember().getMemberName());
            chain.put("uscc", event.getUacMember().getUscCode());
            chain.put("creatTime", LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("MMddHHmmssSSS")));
            chain.put("userType", event.getUserType());
            chain.put("userId", event.getUacStaff().getId().toString());
            chain.put("roleCodeRulesEnum", relevancyTypeId);
            chain.put("chainId", chainId);
            chain.put("userTypeEnum", "管理员");
            mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
            log.info("发送生成员工证书，企业: {} 员工: {}", event.getUacMember().getId(), event.getUacStaff().getId());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}

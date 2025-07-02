package com.aeotrade.provider.rabbitmq;

import com.aeotrade.base.business.sendFormData;
import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.provider.model.UawVipType;
import com.aeotrade.provider.model.UawWorkbench;
import com.aeotrade.provider.service.impl.SmweixinService;
import com.aeotrade.provider.service.UawVipTypeService;
import com.aeotrade.provider.service.UawWorkbenchService;
import com.aeotrade.provider.vo.RegisterOne;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MessageConsumer {
    @Value("${hmtx.crm.url:}")
    private String crmurl;
    private final SmweixinService smweixinService;
    private final MessageProducer messageProducer;
    private final UawVipTypeService uawVipTypeService;
    private final UawWorkbenchService uawWorkbenchService;

    public MessageConsumer(SmweixinService smweixinService, MessageProducer messageProducer, UawVipTypeService uawVipTypeService, UawWorkbenchService uawWorkbenchService) {
        this.smweixinService = smweixinService;
        this.messageProducer = messageProducer;
        this.uawVipTypeService = uawVipTypeService;
        this.uawWorkbenchService = uawWorkbenchService;
    }

    /**
     * 监听线下收录用户信息的队列
     */
    @RabbitListener(queues = "info.formsys.formsysgroup")
    public void receiveMessage(String message) {
        log.info("Received message: {}" , message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        RegisterOne registerOne=new RegisterOne();
        registerOne.setUscCode(jsonObject.getString("uscc"));
        registerOne.setPhone(jsonObject.getString("tel"));
        registerOne.setMemberName(jsonObject.getString("org"));
        registerOne.setStaffName(jsonObject.getString("name"));
        if (jsonObject.get("vipTypeId")!=null&&jsonObject.getString("vipTypeId").startsWith("[")) {
            registerOne.setVipTypeId(jsonObject.getJSONArray("vipTypeId").getLong(0));
        }else {
            registerOne.setVipTypeId(jsonObject.getLong("vipTypeId"));
        }
        registerOne.setSourceMark(jsonObject.getString("formsysId"));
        try {
            UawWorkbench lastWorkbench = null;
            UawVipType uawVipType = uawVipTypeService.getById(registerOne.getVipTypeId());
            if (uawVipType != null) {
                lastWorkbench = uawWorkbenchService.getById(uawVipType.getWorkbench());
            }else {
                log.info("未找到会员类型 {}",registerOne.getVipTypeId());
                return;
            }
            Map<String, Object> map = smweixinService.formDataRegister(registerOne, lastWorkbench,jsonObject);
            messageProducer.sendMessage(JSON.toJSONString(map));
            jsonObject.put("hmm_company_id", map.get("memberId").equals(0L) ? null : map.get("memberId"));
            crmErp(jsonObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Async(AeoConstant.ASYNC_POOL)
    public void crmErp(JSONObject formData) {
        try {
            Map<String, String> companymap = new HashMap<>();
            companymap.put("formData", formData.toJSONString());
            log.info("同步线索数据: {}", JSON.toJSONString(companymap));
            if (!org.springframework.util.StringUtils.isEmpty(crmurl)) {
                sendFormData.erpBus(companymap, crmurl + "/web/receive_crm_lead_create");
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
}

//{
//        "createAt": "2025-06-11 17:26:43",
//        "enroll": 0,
//        "formsysData": [
//        {
//        "id": 1,
//        "sort": 1,
//        "type": "select",
//        "field": "org",
//        "label": "组织名称",
//        "value": "399203",
//        "isShow": true,
//        "required": true,
//        "isSytemField": true
//        },
//        {
//        "id": 2,
//        "sort": 3,
//        "type": "select",
//        "field": "location",
//        "label": "组织所在地",
//        "value": "北京市/市辖区/东城区",
//        "isShow": true,
//        "required": false
//        },
//        {
//        "id": 3,
//        "sort": 5,
//        "type": "select",
//        "field": "industry",
//        "label": "所在行业",
//        "value": "消费产业链/家具家电",
//        "isShow": true,
//        "required": false
//        },
//        {
//        "id": 4,
//        "sort": 7,
//        "type": "text",
//        "field": "name",
//        "label": "您的姓名",
//        "value": "姓名字段",
//        "isShow": true,
//        "required": true
//        },
//        {
//        "id": 5,
//        "sort": 9,
//        "type": "text",
//        "field": "job",
//        "label": "您的职位",
//        "value": "职位",
//        "isShow": true,
//        "required": false
//        },
//        {
//        "id": 6,
//        "sort": 11,
//        "type": "text",
//        "field": "tel",
//        "label": "手机号码",
//        "value": "1301111222",
//        "isShow": true,
//        "required": true,
//        "isSytemField": true
//        },
//        {
//        "id": 7,
//        "sort": 13,
//        "type": "select",
//        "field": "orgRole",
//        "label": "组织角色",
//        "value": {
//        "id": "775791573332918272",
//        "name": "慧贸OS·贸易企业"
//        },
//        "isShow": true,
//        "required": true,
//        "isSytemField": true
//        },
//        {
//        "id": 8,
//        "sort": 15,
//        "type": "text",
//        "field": "chip",
//        "label": "车牌号",
//        "value": "川A12345",
//        "isShow": true,
//        "required": false
//        },
//        {
//        "id": 9,
//        "sort": 17,
//        "type": "select",
//        "field": "source",
//        "label": "报名来源",
//        "value": "专家推荐",
//        "isShow": true,
//        "options": [
//        {
//        "label": "公司活动",
//        "value": "公司活动"
//        },
//        {
//        "label": "专家推荐",
//        "value": "专家推荐"
//        },
//        {
//        "label": "网络了解",
//        "value": "网络了解"
//        }
//        ],
//        "required": false,
//        "canEditOptions": true
//        },
//        {
//        "id": 10,
//        "sort": 19,
//        "type": "text",
//        "field": "idCard",
//        "label": "身份证号码",
//        "value": "510101111122223333",
//        "isShow": true,
//        "required": false
//        },
//        {
//        "id": 11,
//        "sort": 21,
//        "type": "text",
//        "field": "remark",
//        "label": "备注",
//        "value": "备注",
//        "isShow": true,
//        "required": false
//        }
//        ],
//        "formsysId": 13,
//        "id": 11,
//        "jionResult": 0,
//        "msgStatus": 0,
//        "name": "张三",
//        "org": "开鲁县东风镇贾玉凤农储地家庭农场",
//        "orgEnroll": 0,
//        "qichacha": {
//        "id": "372553",
//        "createdAt": 1748955810000,
//        "matchType": "企业名称",
//        "regNo": "150523600252254",
//        "startDate": "2015-09-25",
//        "name": "开鲁县东风镇贾玉凤农储地家庭农场",
//        "operName": "贾玉凤",
//        "creditNo": "92150523MA0NMC8W3Q",
//        "qxbId": "ec6cb4b5-a6f2-4a88-b00a-dc46f56fc80a",
//        "matchItems": "开鲁县东风镇贾玉凤农储地家庭农场",
//        "type": "0"
//        },
//        "registerAeo": 1,
//        "tel": "18108231346",
//        "updateAt": "2025-06-11 17:26:43",
//        "uscc": "92150523MA0NMC8W3Q",
//        "vipTypeId": "775791573332918272"
//        }
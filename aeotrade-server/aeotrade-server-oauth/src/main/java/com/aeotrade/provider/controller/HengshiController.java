package com.aeotrade.provider.controller;

import com.aeotrade.provider.model.UawVipMessage;
import com.aeotrade.provider.model.UawVipType;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.oauth.service.HmtxUserInfoTokenServices;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UawVipMessageService;
import com.aeotrade.provider.service.UawVipTypeService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class HengshiController {
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacStaffService staffService;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Autowired
    private UawVipMessageService uawVipMessageService;
    @GetMapping("user")
    public String user( HttpServletRequest request){

        Map<String,Object> map=new HashMap<>();
        String authorization = request.getHeader("Authorization");
        String token = null;
        if (authorization.contains("bearer ")) {
            token = authorization.replaceAll("bearer ", "");
        }
        if (authorization.contains("Bearer ")) {
            token = authorization.replaceAll("Bearer ", "");
        }
        if (token == null) {
            return null;
        }
        log.debug("接受token:{}" , token);
        OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(token);
        String s = oAuth2Authentication.getPrincipal().toString();
        log.debug("获取用户信息：{}",s);
        Map<String, Object> username = JSONObject.parseObject(s, Map.class);
        log.debug("用户信息转换后：{}", username.toString());
        map.put("loginName",username.get("staffId"));
        map.put("name",username.get("staffName"));
        if(null!=username.get("mobile")){
            map.put("mobile",username.get("mobile"));
        }
        map.put("email",username.get("staffId")+"@aeotrade.com");
        List<String> roleIds=new ArrayList<>();
        roleIds.add("system admin");
        roleIds.add("data admin");
        roleIds.add("data analyst");
        roleIds.add("data viewer");
        map.put("roleIds",roleIds);
        UacStaff uacStaff = staffService.getById((Long) username.get("staffId"));
        if(null!=uacStaff.getLastMemberId()){
            UacMember uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
            if(null!=uacMember){
                map.put("memberId",uacMember.getId());
                map.put("memberName",uacMember.getMemberName());
                map.put("company_id",uacMember.getId());
                map.put("company_credit_code",uacMember.getUscCode());
                map.put("company_name",uacMember.getMemberName());
                List<UawVipMessage> uawVipMessages = uawVipMessageService.lambdaQuery()
                        .eq(UawVipMessage::getMemberId, uacMember.getId())
                        .eq(UawVipMessage::getStatus,0)
                        .eq(UawVipMessage::getUserType,1)
                        .list();
                log.debug("查询会员信息:{}" , uawVipMessages.get(0).getTypeId());
                List<UawVipType> uawVipTypes = uawVipTypeService.lambdaQuery()
                        .in(UawVipType::getId, uawVipMessages.stream().map(UawVipMessage::getTypeId).collect(Collectors.toList())).list();
                UawVipType uawVipType =uawVipTypes.size()>0?uawVipTypes.get(0):null;
                map.put("company_type",uawVipType.getTypeName());
            }
        }
        String jsonString = JSON.toJSONString(map);
        log.info("衡石跳转:{}",jsonString);
        return jsonString;
    }
}

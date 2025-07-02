package com.aeotrade.provider.controller;

import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UawVipMessage;
import com.aeotrade.provider.model.UawVipType;
import com.aeotrade.provider.oauth.service.HmtxUserInfoTokenServices;
import com.aeotrade.provider.service.*;
import com.aeotrade.suppot.BaseController;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther: 吴浩
 * @Date: 2021-09-01 14:02
 */
@RestController
@RequestMapping(value = "/social/")
@Slf4j
public class ThirdpartyController extends BaseController {
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UawVipMessageService uawVipMessageService;
    @Autowired
    private UawVipTypeService uawVipTypeService;



    /**
     * 根据token获取用户信息
     * @param access_token
     * @return
     */
    @GetMapping("user")
    public Map<String, Object> user(String access_token) {
        try {
            OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(access_token);

            log.debug("token信息1 :" + oAuth2Authentication);
            String s = oAuth2Authentication.getPrincipal().toString();
            Map<String, Object> username = JSONObject.parseObject(s, Map.class);
            log.debug("token信息2 :" + username);
            UacStaff uacStaff = uacStaffService.getById((Long) username.get("staffId"));
            Map<String, Object> map = new HashMap<>();
            map.put("mobile", username.get("mobile"));
            map.put("user_name", username.get("staffName"));
            map.put("company_name", username.get("memberName"));
            map.put("company_usc_code", username.get("uscCode"));
            map.put("user_id", username.get("staffId"));
            map.put("is_admin",false);
            if (uacStaff.getLastMemberId() != null) {
                UacMember uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
                if (uacMember != null && (uacMember.getKindId() != 88 || uacMember.getKindId() != 99)) {
                    map.put("company_name", uacMember.getMemberName());
                    map.put("company_usc_code", uacMember.getUscCode());
                    if(uacMember.getStaffId().equals(username.get("staffId"))){
                        map.put("is_admin",true);
                    }
                }
            }
            return map;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("erorr", "token解析错误");
            return map;
        }
    }

    /**
     * 根据token获取用户信息,主要适用APP端展示用户基本资料
     * @param access_token
     * @return
     */
    @GetMapping("/app/user")
    public Map<String, Object> appUser(String access_token) {
        try {
            OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(access_token);

            log.debug("token信息1 :" + oAuth2Authentication);
            String s = oAuth2Authentication.getPrincipal().toString();
            Map<String, Object> username = JSONObject.parseObject(s, Map.class);
            log.debug("token信息2 :" + username);
            UacStaff uacStaff = uacStaffService.getById((Long) username.get("staffId"));
            Map<String, Object> map = new HashMap<>();
            map.put("mobile", username.get("mobile"));
            map.put("user_name", username.get("staffName"));
            map.put("company_name", username.get("memberName"));
            map.put("company_usc_code", username.get("uscCode"));
            map.put("user_id", username.get("staffId"));
            map.put("user_avatar", uacStaff.getWxLogo());
            if (uacStaff.getLastMemberId() != null) {
                UacMember uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
                if (uacMember != null && (uacMember.getKindId() != 88 || uacMember.getKindId() != 99)) {
                    map.put("company_id", uacMember.getId());
                    map.put("company_name", uacMember.getMemberName());
                    map.put("company_usc_code", uacMember.getUscCode());
                }
            }
            return map;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("erorr", "token解析错误");
            return map;
        }
    }
    /**
     * 根据token获取用户信息,主要适用AI系统端展示用户基本资料（私人定制化）
     * @param access_token
     * @return
     */
    @GetMapping("/ai/user")
    public Map<String, Object> aiUser(String access_token) {
        try {
            OAuth2Authentication oAuth2Authentication = hmtxUserInfoTokenServices.loadAuthentication(access_token);

            log.debug("token信息1 :" + oAuth2Authentication);
            String s = oAuth2Authentication.getPrincipal().toString();
            Map<String, Object> username = JSONObject.parseObject(s, Map.class);
            log.debug("token信息2 :" + username);
            UacStaff uacStaff = uacStaffService.getById((Long) username.get("staffId"));
            Map<String, Object> map = new HashMap<>();
            map.put("mobile", username.get("mobile"));
            map.put("user_name", username.get("staffName"));
            map.put("company_name", username.get("memberName"));
            map.put("company_usc_code", username.get("uscCode"));
            map.put("user_id", username.get("staffId"));
            map.put("user_avatar", uacStaff.getWxLogo());
            if (uacStaff.getLastMemberId() != null) {
                UacMember uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
                if (uacMember != null && (uacMember.getKindId() != 88 || uacMember.getKindId() != 99)) {
                    map.put("company_id", uacMember.getId());
                    map.put("company_name", uacMember.getMemberName());
                    map.put("company_usc_code", uacMember.getUscCode());
                    List<UawVipMessage> uawVipMessages = uawVipMessageService.lambdaQuery()
                            .eq(UawVipMessage::getMemberId, uacMember.getId())
                            .eq(UawVipMessage::getStatus,0)
                            .eq(UawVipMessage::getUserType, 1).list();
                    if(!uawVipMessages.isEmpty()){
                        UawVipType uawVipType = uawVipTypeService.getById(uawVipMessages.get(0).getTypeId());
                        if (null!=uawVipType){
                            map.put("company_vip_type", uawVipType.getTypeName());
                        }
                    }
                }
            }
            return map;
        } catch (Exception e) {
            Map<String, Object> map = new HashMap<>();
            map.put("erorr", "token解析错误");
            return map;
        }
    }

}

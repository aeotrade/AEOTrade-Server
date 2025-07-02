package com.aeotrade.provider.controller;

import com.aeotrade.provider.model.*;
import com.aeotrade.provider.oauth.service.HmtxUserInfoTokenServices;
import com.aeotrade.provider.service.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.yulichang.toolkit.MPJWrappers;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * &#064;Auther:  吴浩
 * &#064;Date:  2024-01-19 10:27
 */
@RestController
@Slf4j
@RequestMapping("/bi")
public class BiController{
    @Autowired
    private HmtxUserInfoTokenServices hmtxUserInfoTokenServices;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacStaffService staffService;
    @Autowired
    private UacMemberStaffService uacMemberStaffService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;
    @Autowired
    private UacDeptService uacDeptService;
    @Autowired
    private UawVipClassMenuService uawVipClassMenuService;
    @Autowired
    private UawVipMessageService uawVipMessageService;

    public static List<UacDept> streamMethod(Long parentId, List<UacDept> treeList) {
        List<UacDept> list = new ArrayList<>();
        Optional.ofNullable(treeList).orElse(new ArrayList<>())
                .stream()
                .filter(root -> root.getParentId().equals(parentId))
                .forEach(tree -> {
                    List<UacDept> children = streamMethod(tree.getId(), treeList);
                    tree.setChildren(children);
                    list.add(tree);
                });
        return list;
    }

    @GetMapping("/user")
    public String user( HttpServletRequest request) {
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
        log.debug("获取用户信息：{}", s);
        Map<String, Object> username = JSONObject.parseObject(s, Map.class);
        log.debug("用户信息转换后：{}", username.toString());
        UacStaff uacstaff = staffService.getById((Long) username.get("staffId"));
        UacMember uacmember = uacMemberService.getById(uacstaff.getLastMemberId());
        map.put("staff_id", username.get("staffId"));
        map.put("staff_name", uacstaff.getStaffName());
        map.put("member_id", uacmember.getId());
        map.put("member_uscc", uacmember.getUscCode());
        map.put("member_name", uacmember.getMemberName());

        map.put("is_admin", 0);
        if (uacmember.getStaffId().equals(uacstaff.getId())) {
            map.put("is_admin", 1);
        }
        List<Long> menuIds = uawVipClassMenuService.selectJoinList(UawVipClassMenu.class,
                MPJWrappers.<UawVipClassMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawVipClassMenu.class)
                        .leftJoin(UawVipClass.class, UawVipClass::getId, UawVipClassMenu::getClassId)
                        .leftJoin(UawVipMessage.class, UawVipMessage::getClassSerialNumber, UawVipClass::getClassSerialNumber)
                        .eq(UawVipMessage::getVipStatus, 1)
                        .eq(UawVipMessage::getStatus, 0)
                        .eq(UawVipMessage::getMemberId, uacmember.getId()))
                .stream().map(UawVipClassMenu::getMenuId).collect(Collectors.toList());
        map.put("vip_class_menu", Joiner.on(",").join(menuIds));
        map.put("staff_dept", "");
        map.put("member_dept", "");
        map.put("member_staff", "");
        map.put("member_vip_type", "");
        List<UawVipMessage> uawVipMessages = uawVipMessageService.lambdaQuery()
                .eq(UawVipMessage::getMemberId, uacmember.getId())
                .eq(UawVipMessage::getStatus,0)
                .eq(UawVipMessage::getUserType, 1).list();
        if(!uawVipMessages.isEmpty()){
            map.put("member_vip_type", String.valueOf(uawVipMessages.get(0).getTypeId()));
        }
        List<UacDeptStaff> uacDeptStaffs = uacDeptStaffService.selectJoinList(UacDeptStaff.class,
                MPJWrappers.<UacDeptStaff>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacDeptStaff.class)
                        .leftJoin(UacDept.class, UacDept::getId, UacDeptStaff::getDeptId)
                        .eq(UacDeptStaff::getStaffId, username.get("staffId"))
                        .eq(UacDept::getMemberId, uacmember.getId()));
        if (!uacDeptStaffs.isEmpty()) {
            List<Long> deptIds = uacDeptStaffs.stream().map(UacDeptStaff::getDeptId).collect(Collectors.toList());
            map.put("staff_dept", Joiner.on(",").join(deptIds));
        }
        List<UacDept> deptList = uacDeptService.lambdaQuery()
                .eq(UacDept::getMemberId, uacmember.getId()).orderByAsc(UacDept::getCreatedTime).list();
        if (!deptList.isEmpty()) {
            List<UacDept> uacDepts = streamMethod(uacmember.getId(), deptList);
            map.put("member_dept", uacDepts);
        }
        List<UacMemberStaff> memberId = uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getMemberId, uacmember.getId()).list();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (UacMemberStaff uacMemberStaff : memberId) {
            UacStaff uacStaff = staffService.getById(uacMemberStaff.getStaffId());
            if (null != uacStaff) {
                Map<String, Object> staffMap = new HashMap<>();
                staffMap.put("id", uacStaff.getId());
                staffMap.put("name", uacStaff.getStaffName());
                staffMap.put("is_admin", uacMemberStaff.getIsAdmin());
                staffMap.put("creat_time", uacStaff.getCreatedTime());
                staffMap.put("dept", "");
                List<UacDeptStaff> deptStaffs = uacDeptStaffService.selectJoinList(UacDeptStaff.class,
                        MPJWrappers.<UacDeptStaff>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                .selectAll(UacDeptStaff.class)
                                .leftJoin(UacDept.class, UacDept::getId, UacDeptStaff::getDeptId)
                                .eq(UacDeptStaff::getStaffId, uacStaff.getId())
                                .eq(UacDept::getMemberId, uacmember.getId()));
                if (deptStaffs.size() != 0) {
                    List<Long> deptIds = deptStaffs.stream().map(UacDeptStaff::getDeptId).collect(Collectors.toList());
                    staffMap.put("dept", Joiner.on(",").join(deptIds));
                }
                mapList.add(staffMap);
            }
        }
        map.put("member_staff", mapList);
        return JSON.toJSONString(map);
    }


}


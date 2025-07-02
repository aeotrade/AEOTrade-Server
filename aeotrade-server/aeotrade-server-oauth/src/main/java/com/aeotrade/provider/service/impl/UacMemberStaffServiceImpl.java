package com.aeotrade.provider.service.impl;

import com.aeotrade.base.business.CompanyStaff;
import com.aeotrade.base.business.sendFormData;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.UserDto;
import com.aeotrade.provider.mapper.UacMemberStaffMapper;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.*;
import com.aeotrade.service.MqSend;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.DateUtil;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@Slf4j
public class UacMemberStaffServiceImpl extends ServiceImpl<UacMemberStaffMapper, UacMemberStaff> implements UacMemberStaffService {
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacAdminService uacAdminMapper;
    @Autowired
    private UacAdminRoleService uacAdminRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UacRoleService uacRoleMapper;
    @Value("${hmtx.crm.url:}")
    private String crmurl;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacUserConnectionService uacUserConnectionService;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private UawVipMessageService uawVipMessageService;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Autowired
    private UacDeptService uacDeptService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;


    public void initUacStaffMember(Long staffId, Long pMemberId) {

        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getStaffId, staffId)
                .eq(UacMemberStaff::getMemberId, pMemberId);
        List<UacMemberStaff> uacMemberStaffList = baseMapper.selectList(uacMemberStaffLambdaQueryWrapper);
        if (uacMemberStaffList.size() == 0) {
            UacMemberStaff uacMemberStaff = new UacMemberStaff();
            uacMemberStaff.setStaffId(staffId);
            uacMemberStaff.setMemberId(pMemberId);
            uacMemberStaff.setIsAdmin(0);
            uacMemberStaff.setCreatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            baseMapper.insert(uacMemberStaff);
        }
    }

    public void deleteStaff(Long id) {
        //删除员工表数据
        uacStaffService.removeById(id);
        //删除中间表数据
        LambdaUpdateWrapper<UacMemberStaff> uacMemberStaffLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        uacMemberStaffLambdaUpdateWrapper.eq(UacMemberStaff::getStaffId, id);
        baseMapper.delete(uacMemberStaffLambdaUpdateWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void buildStaffAndMember(Long staffId, Long pMemberId, String staffName, String roleId, String deptId) {
        log.info("进入关联");
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId, pMemberId).eq(UacMemberStaff::getStaffId, staffId);
        List<UacMemberStaff> uacMemberStaffList = baseMapper.selectList(uacMemberStaffLambdaQueryWrapper);
        if (uacMemberStaffList.size() == 0) {
            // 建立企业与员工的关系
            initUacStaffMember(staffId, pMemberId);
        } else {
            throw new AeotradeException("already"); // 前端用already字符来判断弹出提示页面
        }

        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId, staffId)
                .eq(UacAdmin::getStatus, 1)
                .eq(UacAdmin::getIsTab, 1);
        List<UacAdmin> list = uacAdminMapper.list(uacAdminLambdaQueryWrapper);
        UacAdmin uacAdmin = list.size() > 0 ? list.get(0) : null;
        if (uacAdmin == null) {
            throw new RuntimeException("查询账户错误");
        }
        // 设置在企业中的角色
        LambdaQueryWrapper<UacAdminRole> uacAdminRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacAdminRoleLambdaQueryWrapper
                .eq(UacAdminRole::getMemberId, pMemberId)
                .eq(UacAdminRole::getAdminId, uacAdmin.getId());
        List<UacAdminRole> byOrgIdAndAdminId = uacAdminRoleMapper.list(uacAdminRoleLambdaQueryWrapper);
        if (!byOrgIdAndAdminId.isEmpty()) {
            for (UacAdminRole uacAdminRole : byOrgIdAndAdminId) {
                uacAdminRoleMapper.removeById(uacAdminRole);
            }

        }
        List<String> roleName = new ArrayList<>();
        if (StringUtils.isNotEmpty(roleId)) {
            String[] split = roleId.split(",");
            for (String s : split) {
                UacRole uacRole = uacRoleMapper.getById(s);
                if (null != uacRole) {
                    roleName.add(uacRole.getName());
                    UacAdminRole uacAdminRole = new UacAdminRole();
                    uacAdminRole.setMemberId(pMemberId);
                    uacAdminRole.setOrgi(String.valueOf(uacRole.getPlatformId()));
                    uacAdminRole.setAdminId(uacAdmin.getId());
                    uacAdminRole.setRoleId(Long.valueOf(s));
                    uacAdminRoleMapper.save(uacAdminRole);
                }
            }
        }
        List<String> deptName = new ArrayList<>();
        if (StringUtils.isNotEmpty(deptId)) {
            String[] split = deptId.split(",");
            for (String s : split) {
                UacDept uacDept = uacDeptService.getById(s);
                if (null != uacDept) {
                    deptName.add(uacDept.getDeptName());
                    UacDeptStaff uacDeptStaff = new UacDeptStaff();
                    uacDeptStaff.setDeptId(Long.valueOf(s));
                    uacDeptStaff.setStaffId(staffId);
                    uacDeptStaffService.save(uacDeptStaff);
                }
            }
        }
        log.info("开始生成消息++++++++++++++++++++++++++++++++++++");
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    HashMap<String, String> messageMap = new HashMap<>();
                    UacMember uacMember = uacMemberService.getById(pMemberId);
                    messageMap.put("receive_id", String.valueOf(uacMember.getStaffId()));
                    UacStaff uacStaff = uacStaffService.getById(uacMember.getStaffId());
                    messageMap.put("receive_name", uacStaff.getStaffName());
                    messageMap.put("receive_type", "1");
                    messageMap.put("details_type", "3");
                    messageMap.put("details_button", "立即前往");
//                    messageMap.put("message_details",messageDetails);
                    messageMap.put("message_source", "基础平台");
                    messageMap.put("name", staffName);

                    messageMap.put("dept", "无");
                    if (StringUtils.isNotEmpty(deptId)) {
                        messageMap.put("dept", Joiner.on(",").join(deptName));
                    }
                    messageMap.put("role", "无");
                    if (StringUtils.isNotEmpty(roleId)) {
                        messageMap.put("role", Joiner.on(",").join(roleName));
                    }
                    messageMap.put("template_number", "GG2024000004");
                    mqSend.sendMessage(JSONObject.toJSONString(messageMap), "GG2024000004");

                    messageMap.put("receive_id", String.valueOf(staffId));
                    messageMap.put("receive_name", staffName);
                    messageMap.put("memberName", uacMember.getMemberName());
                    messageMap.put("template_number", "GG2024000003");
                    mqSend.sendMessage(JSONObject.toJSONString(messageMap), "GG2024000003");
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
    }

    public int upadteAdmin(UserDto userDto) throws Exception {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getUsername, userDto.getUserName()).eq(UacAdmin::getIsTab, 1).ne(UacAdmin::getStatus, 1);
        List<UacAdmin> userByName = uacAdminMapper.list(uacAdminLambdaQueryWrapper);
        if (userByName.size() != 0) {
            throw new AeotradeException("该账号已被注册");
        }
        String encode = passwordEncoder.encode(userDto.getPassWord());
        log.info(encode);
        uacAdminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId, userDto.getStaffId()).eq(UacAdmin::getStatus, 1).eq(UacAdmin::getIsTab, 1);
        UacAdmin list = uacAdminMapper.list(uacAdminLambdaQueryWrapper).get(0);
        if (CommonUtil.isEmpty(list)) {
            UacAdmin uacAdmin = new UacAdmin();
            uacAdmin.setUsername(userDto.getUserName());
            uacAdmin.setPassword(encode);
            uacAdmin.setStatus(1);
            uacAdmin.setIsTab(1);
            uacAdmin.setCreateTime(LocalDateTime.now());
            uacAdmin.setUpdateTime(LocalDateTime.now());
            uacAdmin.setStaffId(userDto.getStaffId());
            Boolean i = uacAdminMapper.save(uacAdmin);
            return i ? 1 : 0;
        }

        if (!CommonUtil.isEmpty(list) && list.getUsername() == null) {
            LambdaUpdateWrapper<UacAdmin> uacAdminLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            uacAdminLambdaUpdateWrapper.set(UacAdmin::getUsername, userDto.getUserName()).set(UacAdmin::getPassword, userDto.getPassWord())
                    .set(UacAdmin::getUpdateTime, LocalDateTime.now()).eq(UacAdmin::getId, list.getId());
            Boolean i = uacAdminMapper.update(uacAdminLambdaUpdateWrapper);
            return i ? 1 : 0;
        }
        if (!CommonUtil.isEmpty(list) && list.getUsername().equals(userDto.getUserName())) {
            throw new AeotradeException("该用户已经绑定用户名密码");
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public UacStaff initUacStaffAndUacMember(UacUserConnection uacUserConnection, Long pStaffId, Long pMemberId, String phone) {
        try {
            //初始化 企业信息
            UacMember uacMember = new UacMember();
            uacMember.setStatus(BizConstant.DEL_FLAG_NO);
            uacMember.setAtpwStatus(0);
            // uacMember.setPersonageStatus(0);
            uacMember.setCreatedTime(DateUtil.getData().toLocalDateTime());
            uacMember.setKindId(Long.valueOf(BizConstant.MemberKindEnum.VISITOR_KINDID.getValue()));
            uacMember.setSgsStatus(0);
            if (phone != null) {
                uacMember.setStasfTel(phone);
            }
            uacMemberService.save(uacMember);
            Long memberId = uacMember.getId();

            //初始化 员工信息
            UacStaff uacStaff = new UacStaff();
            uacStaff.setCreatedTime(DateUtil.getData().toLocalDateTime());
            uacStaff.setStatus(BizConstant.DEL_FLAG_NO);
            uacStaff.setMemberId(memberId);
            uacStaff.setStaffName(uacUserConnection.getDisplayName());
            uacStaff.setWxLogo(uacUserConnection.getImageUrl());
            uacStaff.setWxOpenid(uacUserConnection.getProviderUserId());
            uacStaff.setWxUnionid(uacUserConnection.getUnionid());
            uacStaff.setSgsStatus(0);
            uacStaff.setStaffType(BizConstant.StaffTypeEnum.PERSONAL.getValue());
            uacStaff.setSourceMark("weixin注册");
//            uacStaff.setChannelMark("");
            uacStaff.setLastWorkbenchId(1L);
            uacStaff.setChannelColumnsId(0L);
            if (pStaffId != null) {
                uacStaff.setCreatedBy(pStaffId.toString());
                uacStaff.setSgsStatus(1);
                uacStaff.setStaffType(BizConstant.StaffTypeEnum.ENTERPRISE.getValue());
            }
            uacStaff.setAuthStatus(0);
            uacStaff.setIsLogin(0);
            if (phone != null) {
                uacStaff.setTel(phone);
            }
            uacStaffService.save(uacStaff);
            //0个人1企业

            Long staffId = uacStaff.getId();
            //绑定最高级管理员
            uacMember.setStaffId(staffId);
            uacMember.setRevision(1);
            uacMemberService.updateById(uacMember);
            //添加企业关联
            if (pMemberId != null) {
                initUacStaffMember(staffId, pMemberId);
            } else {
                initUacStaffMember(staffId, uacMember.getId());
            }
            //将微信登录信息添加员工ID
            UacUserConnection con = new UacUserConnection();
            con.setId(uacUserConnection.getId());
            con.setStaffId(staffId);
            uacUserConnectionService.updateById(con);
            return uacStaff;
        } catch (Exception e) {
            throw e;
        }

    }

    public void update(Long staffId, Long memberId, String memberName, String uscc) {
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId, memberId).eq(UacMemberStaff::getStaffId, staffId);
        List<UacMemberStaff> uacMemberStaffs = baseMapper.selectList(uacMemberStaffLambdaQueryWrapper);
        if (null == uacMemberStaffs || uacMemberStaffs.size() == 0) {
            UacMemberStaff uacMemberStaff = new UacMemberStaff();
            uacMemberStaff.setMemberId(memberId);
            uacMemberStaff.setStaffId(staffId);
            uacMemberStaff.setIsAdmin(0);
            uacMemberStaff.setCreatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
            baseMapper.insert(uacMemberStaff);
            HashMap<String, String> chain = new HashMap<>();
            chain.put("tenantId", String.valueOf(memberId));
            chain.put("tenantName", memberName);
            chain.put("uscc", uscc);
            SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
            chain.put("creatTime", SDFormat.format(new Date()));
            chain.put("userType", "员工");
            chain.put("userId", String.valueOf(staffId));
            List<UawVipMessage> member = uawVipMessageService.lambdaQuery().eq(UawVipMessage::getMemberId, memberId).list();
            if (member != null || member.size() != 0) {
                UawVipType vipType = uawVipTypeService.getById(member.get(0).getTypeId());
                if (null != vipType) {
                    chain.put("roleCodeRulesEnum", vipType.getRelevancyTypeId());
                } else {
                    chain.put("roleCodeRulesEnum", "01");
                }
            }
            chain.put("chainId", "aeotradechain");
            chain.put("userTypeEnum", "员工");
            mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
        }

    }
}

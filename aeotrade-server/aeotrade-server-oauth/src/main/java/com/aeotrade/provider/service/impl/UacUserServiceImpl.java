package com.aeotrade.provider.service.impl;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.event.MemberAuthEvent;
import com.aeotrade.provider.mapper.*;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.UacUserService;
import com.aeotrade.provider.service.UawVipTypeService;
import com.aeotrade.provider.util.MgLogEntity;
import com.aeotrade.service.MqSend;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@Slf4j
public class UacUserServiceImpl extends ServiceImpl<UacUserMapper, UacUser> implements UacUserService {
    @Autowired
    UacAdminMapper uacAdminMapper;
    @Autowired
    UacStaffMapper uacStaffMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private UacUserConnectionMapper uacUserConnectionMapper;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private UacMemberStaffMapper uacMemberStaffMapper;
    @Autowired
    private UacMemberMapper uacMemberMapper;
    @Autowired
    private UawVipMessageMapper uawVipMessageMapper;
    @Autowired
    private UawVipTypeService uawVipTypeService;
    @Transactional
    public void bindingMobile(String mobile, Long staffId, String staffname, String url) throws Exception {
        log.info("要绑定手机号了++++++++++++++++++++++++++++++++++++++++++++++");
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,mobile).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1)
                .orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin byMobile = uacAdmins.size()>0?uacAdmins.get(0):null;
        if (!CommonUtil.isEmpty(byMobile)) throw new AeotradeException("该手机号已经绑定");
        uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId,staffId).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1)
                .orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> admins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin byStaffId =admins.size()>0?admins.get(0):null;
        if (byStaffId == null) {
            UacAdmin uacAdmin=new UacAdmin();
            uacAdmin.setMobile(mobile);
            uacAdmin.setStatus(1);
            uacAdmin.setIsTab(1);
            uacAdmin.setCreateTime(LocalDateTime.now());
            uacAdmin.setUpdateTime(LocalDateTime.now());
            uacAdmin.setStaffId(staffId);
            uacAdminMapper.insert(uacAdmin);
        } else {
            byStaffId.setMobile(mobile);
            LambdaUpdateWrapper<UacAdmin> uacAdminLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
            uacAdminLambdaUpdateWrapper.set(UacAdmin::getMobile,byStaffId.getMobile()).eq(UacAdmin::getId,byStaffId.getId());
            uacAdminMapper.update(uacAdminLambdaUpdateWrapper);
        }
        UacStaff staff = uacStaffMapper.selectById(staffId);
        staff.setId(staffId);
        staff.setTel(mobile);
        if (StringUtils.isNotEmpty(staffname)) {
            staff.setStaffName(staffname);
        }
        if (StringUtils.isNotEmpty(url)) {
            staff.setWxLogo(url);
        }
        uacStaffMapper.updateById(staff);
        MgLogEntity mgLogEntity = new MgLogEntity();
        mgLogEntity.setStaffId(String.valueOf(staff.getId()));
        mgLogEntity.setName(staff.getStaffName());
        mgLogEntity.setTel(staff.getTel());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        mgLogEntity.setTime(format);
        mgLogEntity.setQudao(staff.getSourceMark());
        log.info("要发邮件了++++++++++++++++++++++++++++++++++++++++++++++");
        MemberAuthEvent memberAuthEvent=new MemberAuthEvent(this,mgLogEntity);
        eventPublisher.publishEvent(memberAuthEvent);

    }

    public Integer findByMobile(String mobile) {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,mobile).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1)
                .orderByDesc(UacAdmin::getCreateTime);

        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin uacUser =uacAdmins.size()>0?uacAdmins.get(0):null;
        if (CommonUtil.isEmpty(uacUser)) {
            return 0;
        } else {
            UacStaff uacStaff = uacStaffMapper.selectById(uacUser.getStaffId());
            if (uacStaff == null || StringUtils.isEmpty(uacStaff.getWxOpenid())) {
                return 0;
            }
        }
        return 1;
    }

    public int memberMobile(Long memberId, String phone) {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,phone).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1)
                .orderByDesc(UacAdmin::getCreateTime);
        List<UacAdmin> uacAdmins = uacAdminMapper.selectList(uacAdminLambdaQueryWrapper);
        UacAdmin uacUser = uacAdmins.size()>0?uacAdmins.get(0):null;
        if (CommonUtil.isEmpty(uacUser)) {
            return 0;
        } else {
            UacStaff uacStaff = uacStaffMapper.selectById(uacUser.getStaffId());
            if (null != uacStaff) {
                LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
                uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId,memberId).eq(UacMemberStaff::getStaffId,uacStaff.getId());
                List<UacMemberStaff> staffIdMemberID = uacMemberStaffMapper.selectList(uacMemberStaffLambdaQueryWrapper);
                if (null != staffIdMemberID && staffIdMemberID.size() != 0) {
                    return 1;
                } else {
                    UacMemberStaff uacMemberStaff = new UacMemberStaff();
                    uacMemberStaff.setStaffId(uacStaff.getId());
                    uacMemberStaff.setMemberId(memberId);
                    uacMemberStaffMapper.insert(uacMemberStaff);
                    ThreadPoolUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                UacMember uacMember = uacMemberMapper.selectById(memberId);
                                HashMap<String, String> chain = new HashMap<>();
                                chain.put("tenantId", String.valueOf(memberId));
                                chain.put("tenantName", uacMember.getMemberName());
                                chain.put("uscc", uacMember.getUscCode());
                                SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
                                chain.put("creatTime", SDFormat.format(uacMember.getCreatedTime()));
                                chain.put("userType", "员工");
                                chain.put("userId", String.valueOf(uacStaff.getId()));
                                LambdaQueryWrapper<UawVipMessage> uawVipMessageLambdaQueryWrapper=new LambdaQueryWrapper<>();
                                uawVipMessageLambdaQueryWrapper.eq(UawVipMessage::getMemberId,memberId);
                                List<UawVipMessage> member = uawVipMessageMapper.selectList(uawVipMessageLambdaQueryWrapper);
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
                            } catch (Exception e) {
                                log.warn(e.getMessage());
                            }
                        }
                    });
                    return 2;
                }
            } else {
                return 3;
            }
        }
    }

    @Transactional
    public void LoginBanding(String mobile, Long staffId, String staffname, String url) throws Exception {
        //扫码登录或账号密码登录的用户id
        UacStaff uacStaff = uacStaffMapper.selectById(staffId);
        if (null == uacStaff) {
            throw new AeotradeException("用户信息为空");
        }
        //要绑定的手机号
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getMobile,mobile).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1)
                .orderByDesc(UacAdmin::getCreateTime);
        UacAdmin uacAdmin = uacAdminMapper.selectOne(uacAdminLambdaQueryWrapper);
        //如果要绑定的手机号查到数据
        if (uacAdmin != null) {
            //就根据查出的UacAdmin里的用户id查询用户信息
            UacStaff staff = uacStaffMapper.selectById(uacAdmin.getStaffId());
            //如果用户信息不为空
            if (staff != null && staff.getWxOpenid() == null) {
                LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
                uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getStaffId,staffId);
                List<UacMemberStaff> listByStaffId = uacMemberStaffMapper.selectList(uacMemberStaffLambdaQueryWrapper);
                if (listByStaffId.size() > 0) {
                    //查询手机号绑定用户的所有企业
                    List<UacMember> memberList = uacStaffMapper.selectJoinList(UacMember.class,new MPJLambdaWrapper<UacStaff>()
                            .selectAll(UacMember.class)
                            .leftJoin(UacMemberStaff.class,UacMemberStaff::getStaffId,UacStaff::getId)
                            .leftJoin(UacMember.class,UacMember::getId,UacMemberStaff::getMemberId)
                                            .eq(UacStaff::getId,uacAdmin.getStaffId())
                                    .ne(UacMember::getStatus,1)
                                    .notIn(UacMember::getKindId,88,99)
                            );
                    for (UacMember uacMember : memberList) {
                        uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId,uacMember.getId())
                                .eq(UacMemberStaff::getStaffId,staff.getId());
                        UacMemberStaff staMem = uacMemberStaffMapper.selectOne(uacMemberStaffLambdaQueryWrapper);
                        uacMember.setStaffId(staffId);
                        uacMember.setRevision(0);
                        uacMemberMapper.updateById(uacMember);
                        staMem.setStaffId(staffId);
                        uacMemberStaffMapper.updateById(staMem);
                    }
                    LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacMemberLambdaQueryWrapper.eq(UacMember::getStaffId,staff.getId()).eq(UacMember::getKindId,99);
                    List<UacMember> byKindId = uacMemberMapper.selectList(uacMemberLambdaQueryWrapper);
                    if (byKindId != null) {
                        for (UacMember uacMember : byKindId) {
                            LambdaUpdateWrapper<UacMemberStaff> uacMemberStaffLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                            uacMemberStaffLambdaUpdateWrapper.eq(UacMemberStaff::getStaffId,uacMember.getStaffId());
                            uacMemberStaffMapper.delete(uacMemberStaffLambdaUpdateWrapper);
                            uacMemberMapper.deleteById(uacMember.getId());
                        }
                    }
                    uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    uacAdminLambdaQueryWrapper.eq(UacAdmin::getStaffId,staffId).eq(UacAdmin::getStatus,1)
                            .eq(UacAdmin::getIsTab,1);
                    UacAdmin admin = uacAdminMapper.selectOne(uacAdminLambdaQueryWrapper);
                    if (admin != null) {
                        LambdaUpdateWrapper<UacAdmin> uacAdminLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                        uacAdminLambdaUpdateWrapper.set(UacAdmin::getMobile,mobile).eq(UacAdmin::getId,admin.getId());
                        uacAdminMapper.update(uacAdminLambdaUpdateWrapper);
                        uacAdminMapper.deleteById(uacAdmin.getId());
                    } else {
                        LambdaUpdateWrapper<UacAdmin> uacAdminLambdaUpdateWrapper=new LambdaUpdateWrapper<>();
                        uacAdminLambdaUpdateWrapper.set(UacAdmin::getStaffId,staffId).eq(UacAdmin::getId,uacAdmin.getId());
                        uacAdminMapper.update(uacAdminLambdaUpdateWrapper);
                    }
                    if (StringUtils.isNotEmpty(staff.getLoginAccount())) {
                        uacStaff.setLoginAccount(staff.getLoginAccount());
                    }
                    uacStaff.setTel(mobile);
                    uacStaffMapper.updateById(uacStaff);
                    uacStaffMapper.deleteById(staff);
                } else {
                    log.info("用户查询id+++++++++++++++++++++++++++" + staffId);
                    throw new AeotradeException("微信或手机号已被使用，绑定失败");
                }

            } else {
                throw new AeotradeException("该手机号已被绑定");
            }
        } else {
            //如果要绑定的手机号没有查到数据直接按正常绑定走
            this.bindingMobile(mobile, staffId, staffname, url);
        }
    }


}

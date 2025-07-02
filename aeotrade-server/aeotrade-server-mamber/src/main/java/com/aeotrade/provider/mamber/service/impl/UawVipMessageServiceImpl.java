package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.dingding.SendTextMessage;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.utils.DateUlit;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.feign.AdminFeign;
import com.aeotrade.provider.mamber.mapper.*;
import com.aeotrade.provider.mamber.service.UawVipMessageService;
import com.aeotrade.provider.mamber.vo.*;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 会员信息表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawVipMessageServiceImpl extends ServiceImpl<UawVipMessageMapper, UawVipMessage> implements UawVipMessageService {
    @Autowired
    private UawVipClassServiceImpl uawVipClassMapper;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeMapper;
    @Autowired
    private UawRightsTypeServiceImpl uawRightsTypeMapper;
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupMapper;
    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchMapper;
    @Autowired
    private UawVipPostponeServiceImpl uawVipPostponeMapper;
    @Autowired
    private UawAptitudesServiceImpl uawAptitudesService;
    @Autowired
    private UawVipClassMenuServiceImpl uawVipClassMenuMapper;
    @Autowired
    private UawWorkbenchMenuServiceImpl uawWorkbenchMenuMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberService;
    @Autowired
    private UacStaffServiceImpl uacStaffService;
    @Autowired
    private UacRoleServiceImpl uacRoleService;

    @Autowired
    private AdminFeign adminFeign;
    @Value("${hmtx.dingding.secret:}")
    private String secret;
    @Value("${hmtx.dingding.token:}")
    private String token;

    public VipMessageVos findPageAll(int pageSize, int pageNo, int apply, String name, Long group, Long typeId) {
        if (apply == 0) {
            if (StringUtils.isNotEmpty(name)) {
                LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
                uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
                uacStaffLambdaQueryWrapper.like(UacStaff::getStaffName, name);
                Page<UacStaff> uacStaffPage = uacStaffService.page(new Page<>(pageNo, pageSize), uacStaffLambdaQueryWrapper);
                if(uacStaffPage.getRecords().isEmpty()){
                    return new VipMessageVos(Math.toIntExact(uacStaffPage.getTotal()), new ArrayList<>());
                }
                MPJLambdaWrapper<UawVipMessage> uawVipMessageMPJLambdaWrapper = MPJWrappers.<UawVipMessage>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawVipMessage.class)
                        .select(UawVipType::getTypeName)
                        .selectAs(UawVipType::getGroupId, "groupId")
                        .selectAs(UawVipTypeGroup::getGroupName, "groupName")
                        .selectAs(UawVipClass::getClassName, "className")
                        .leftJoin(UawVipClass.class, UawVipClass::getClassSerialNumber, UawVipMessage::getClassSerialNumber)
                        .leftJoin(UawVipType.class, UawVipType::getId, UawVipMessage::getTypeId)
                        .leftJoin(UawVipTypeGroup.class, UawVipTypeGroup::getId, UawVipType::getGroupId)
                        .eq(UawVipMessage::getStatus, 0)
                        .eq(UawVipClass::getStatus, 0)
                        .eq(UawVipTypeGroup::getApply, apply)
                        .in(UawVipMessage::getStaffId, uacStaffPage.getRecords().stream().map(UacStaff::getId).collect(Collectors.toList()));
                if(group!=0){
                    uawVipMessageMPJLambdaWrapper.eq(UawVipType::getGroupId, group);
                }
                if(typeId!=0){
                    uawVipMessageMPJLambdaWrapper.eq(UawVipClass::getTypeId, typeId);
                }
                List<UawVipMessageVo> uawVipMessageVos = this.selectJoinList(UawVipMessageVo.class,uawVipMessageMPJLambdaWrapper);
                for (UawVipMessageVo uawVipMessageVo : uawVipMessageVos) {
                    for (UacStaff record : uacStaffPage.getRecords()) {
                        if(uawVipMessageVo.getStaffId().equals(record.getId())){
                            uawVipMessageVo.setName(record.getStaffName());
                            uawVipMessageVo.setEmail(record.getContactEmail());
                            uawVipMessageVo.setTel(record.getTel());
                        }
                    }
                }

                return new VipMessageVos(Math.toIntExact(uacStaffPage.getTotal()), uawVipMessageVos);
            }else{
                MPJLambdaWrapper<UawVipMessage> uawVipMessageMPJLambdaWrapper = MPJWrappers.<UawVipMessage>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawVipMessage.class)
                        .select(UawVipType::getTypeName)
                        .selectAs(UawVipType::getGroupId, "groupId")
                        .selectAs(UawVipTypeGroup::getGroupName, "groupName")
                        .selectAs(UawVipClass::getClassName, "className")
                        .leftJoin(UawVipClass.class, UawVipClass::getClassSerialNumber, UawVipMessage::getClassSerialNumber)
                        .leftJoin(UawVipType.class, UawVipType::getId, UawVipMessage::getTypeId)
                        .leftJoin(UawVipTypeGroup.class, UawVipTypeGroup::getId, UawVipType::getGroupId)
                        .eq(UawVipMessage::getStatus, 0)
                        .eq(UawVipClass::getStatus, 0)
                        .eq(UawVipTypeGroup::getApply, apply);
                if(group!=0){
                    uawVipMessageMPJLambdaWrapper.eq(UawVipType::getGroupId, group);
                }
                if(typeId!=0){
                    uawVipMessageMPJLambdaWrapper.eq(UawVipClass::getTypeId, typeId);
                }
                Page<UawVipMessageVo> uawVipMessageVoPage = this.selectJoinListPage(new Page<>(pageNo, pageSize), UawVipMessageVo.class,uawVipMessageMPJLambdaWrapper);
                for (UawVipMessageVo record : uawVipMessageVoPage.getRecords()) {
                    UacStaff uacStaff = uacStaffService.getById(record.getStaffId());
                    if(null!=uacStaff){
                        record.setName(uacStaff.getStaffName());
                        record.setEmail(uacStaff.getContactEmail());
                        record.setTel(uacStaff.getTel());
                        record.setSgsStatus(uacStaff.getAuthStatus());
                    }
                }
                return new VipMessageVos(Math.toIntExact(uawVipMessageVoPage.getTotal()), uawVipMessageVoPage.getRecords());
            }
        }
        if (apply == 1) {
            if (StringUtils.isNotEmpty(name)) {
                LambdaQueryWrapper<UacMember> uacmemberLambdaQueryWrapper = new LambdaQueryWrapper<>();
                uacmemberLambdaQueryWrapper.eq(UacMember::getStatus, 0);
                uacmemberLambdaQueryWrapper.ne(UacMember::getKindId, 88L);
                uacmemberLambdaQueryWrapper.ne(UacMember::getKindId, 99L);
                uacmemberLambdaQueryWrapper.like(UacMember::getMemberName, name);
                Page<UacMember> uacmemberPage = uacMemberService.page(new Page<>(pageNo, pageSize), uacmemberLambdaQueryWrapper);
                if(!uacmemberPage.getRecords().isEmpty()){
                    MPJLambdaWrapper<UawVipMessage> vipMessageMPJLambdaWrapper = MPJWrappers.<UawVipMessage>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawVipMessage.class)
                            .select(UawVipType::getTypeName)
                            .selectAs(UawVipType::getGroupId, "groupId")
                            .selectAs(UawVipTypeGroup::getGroupName, "groupName")
                            .selectAs(UawVipClass::getClassName, "className")
                            .leftJoin(UawVipClass.class, UawVipClass::getClassSerialNumber, UawVipMessage::getClassSerialNumber)
                            .leftJoin(UawVipType.class, UawVipType::getId, UawVipMessage::getTypeId)
                            .leftJoin(UawVipTypeGroup.class, UawVipTypeGroup::getId, UawVipType::getGroupId)
                            .in(UawVipMessage::getMemberId, uacmemberPage.getRecords().stream().map(UacMember::getId).collect(Collectors.toList()))
                            .eq(UawVipMessage::getStatus, 0)
                            .eq(UawVipClass::getStatus, 0)
                            .eq(UawVipTypeGroup::getApply, apply);
                    if(group!=0){
                        vipMessageMPJLambdaWrapper.eq(UawVipType::getGroupId, group);
                    }
                    if(typeId!=0){
                        vipMessageMPJLambdaWrapper.eq(UawVipClass::getTypeId, typeId);
                    }
                    List<UawVipMessageVo> uawVipMessageVos = this.selectJoinList(UawVipMessageVo.class,vipMessageMPJLambdaWrapper);
                    for (UawVipMessageVo uawVipMessageVo : uawVipMessageVos) {
                        for (UacMember record : uacmemberPage.getRecords()) {
                            if (uawVipMessageVo.getMemberId().equals(record.getId())) {
                                uawVipMessageVo.setName(record.getMemberName());
                                uawVipMessageVo.setStaffName(record.getStaffName());
                                uawVipMessageVo.setEmail(record.getEmail());
                                uawVipMessageVo.setTel(record.getStasfTel());
                            }
                        }
                    }
                    return new VipMessageVos(Math.toIntExact(uacmemberPage.getTotal()), uawVipMessageVos);
                }
                return new VipMessageVos(Math.toIntExact(uacmemberPage.getTotal()), new ArrayList<>());
            } else {
                MPJLambdaWrapper<UawVipMessage> vipMessageMPJLambdaWrapper = MPJWrappers.<UawVipMessage>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawVipMessage.class)
                        .select(UawVipType::getTypeName)
                        .selectAs(UawVipType::getGroupId, "groupId")
                        .selectAs(UawVipTypeGroup::getGroupName, "groupName")
                        .selectAs(UawVipClass::getClassName, "className")
                        .leftJoin(UawVipClass.class, UawVipClass::getClassSerialNumber, UawVipMessage::getClassSerialNumber)
                        .leftJoin(UawVipType.class, UawVipType::getId, UawVipMessage::getTypeId)
                        .leftJoin(UawVipTypeGroup.class, UawVipTypeGroup::getId, UawVipType::getGroupId)
                        .eq(UawVipMessage::getStatus, 0)
                        .eq(UawVipClass::getStatus, 0)
                        .eq(UawVipTypeGroup::getApply, apply);
                if(group!=0){
                    vipMessageMPJLambdaWrapper.eq(UawVipType::getGroupId, group);
                }
                if(typeId!=0){
                    vipMessageMPJLambdaWrapper.eq(UawVipClass::getTypeId, typeId);
                }
                Page<UawVipMessageVo> uawVipMessageVoPage = this.selectJoinListPage(new Page<>(pageNo, pageSize), UawVipMessageVo.class,vipMessageMPJLambdaWrapper);
                for (UawVipMessageVo record : uawVipMessageVoPage.getRecords()) {
                    UacMember uacMember = uacMemberService.getById(record.getMemberId());
                    if(null!=uacMember){
                        record.setName(uacMember.getMemberName());
                        record.setStaffName(uacMember.getStaffName());
                        record.setEmail(uacMember.getEmail());
                        record.setTel(uacMember.getStasfTel());
                    }
                }
                return new VipMessageVos(Math.toIntExact(uawVipMessageVoPage.getTotal()), uawVipMessageVoPage.getRecords());
            }
        }
        return null;
    }


    /**
     * @description: smId:个人/企业id   id：会员等级id  orderType：会员类型个人/企业  vipid：会员类型id  endTime:会员开通到期时间
     * @return: Boolean
     * @author: wuhao
     * @date:
     */
    public Boolean updateMessage(String SmId, String id, String orderType, String vipId, LocalDateTime endTime, String type) throws ParseException {
        //获取当前时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        if (SmId == null) {
            throw new AeotradeException("用户id为空");
        }
        if (id == null) {
            throw new AeotradeException("会员等级编号为空");
        }
        if (orderType == null) {
            throw new AeotradeException("订单类型为空");
        }
        if (vipId == null) {
            throw new AeotradeException("会员类型id为空");
        }
        //根据企业还是个人id加会员类型id两个条件获取当前会员类型的会员信息
        List<UawVipMessage> list = this.lambdaQuery().eq(UawVipMessage::getTypeId, Long.valueOf(vipId))
                .eq(orderType.equals("0") ? UawVipMessage::getStaffId : UawVipMessage::getMemberId, Long.valueOf(SmId))
                .eq(UawVipMessage::getStatus, 0).list();
        UawVipMessage entity =list.size()>0?list.get(0):null;
        if ("续费".equals(type) && entity.getVipStatus() != 0) {
            entity.setEndTime(this.timecompute(entity.getEndTime(), endTime));
            this.saveOrUpdate(entity);
            return true;
        }
        //添加会员等级id
        entity.setClassSerialNumber(id);
        //添加会员开始时间（获取当前时间）
        entity.setStartTime(LocalDateTime.now());
        //添加会员结束时间（根据会员订单传值）
        entity.setEndTime(endTime);
        //修改会员状态（0到期/未启用1使用中）
        entity.setVipStatus(1);
        //执行修改操作
        boolean update = this.saveOrUpdate(entity);
        if (update) {
            if (orderType.equals("1")) {
                ThreadPoolUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String format = df.format(new Date());
                            UacMember memberById = uacMemberService.getById(entity.getMemberId());
                            if (null != memberById) {
                                UawVipType uawVipType = uawVipTypeMapper.getById(Long.valueOf(vipId));
                                UawVipTypeGroup uawVipTypeGroup = uawVipTypeGroupMapper.getById(uawVipType.getGroupId());
                                List<UawVipClass> uawVipClasses = uawVipClassMapper.lambdaQuery().eq(UawVipClass::getClassSerialNumber, id).list();
                                UawVipClass uawVipClass =uawVipClasses.size()>0?uawVipClasses.get(0):null;
                                String main = "用户已开通:" + uawVipType.getTypeName() + "·" + uawVipClass.getClassName() + "\n" +
                                        "企业名称:" + memberById.getMemberName() + "\n" +
                                        "社会统一信用代码:" + memberById.getUscCode() + "\n" +
                                        "慧贸OS角色:" + uawVipTypeGroup.getGroupName() + "\n" +
                                        "时间:" + format + "\n";
                                if (!org.springframework.util.StringUtils.isEmpty(secret)) {
                                    SendTextMessage.sendWithAtAll(main, token, secret);
                                }
                            }
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    }
                });
            }
            System.out.println(entity + "--------------------------------");
            return true;
        } else {
            System.out.println("执行失败开始回滚");
            //执行失败回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return false;
        }
    }

    /**
     * @description: 前端会员中心首页展示该用户的会员类型、会员等级、权益类型
     * @return:
     * @author: wuhao
     * @date:
     */
    public List<homePageVo> findMessage(Long id, int apply) throws Exception {
        List<UawVipMessage> list = this.lambdaQuery()
                .eq(UawVipMessage::getUserType, apply)
                .eq(apply == 0 ? UawVipMessage::getStaffId : UawVipMessage::getMemberId, id)
                .eq(UawVipMessage::getStatus, 0).list();
        List<homePageVo> homePageVolist = new ArrayList<>();
        if (!CommonUtil.isEmpty(list)) {
            for (UawVipMessage vipMessage : list) {
                homePageVo homePageVo = new homePageVo();
                homePageVo.setEntTime(vipMessage.getEndTime());
                homePageVo.setVipstatus(vipMessage.getVipStatus());
                //会员类型
                List<UawVipType> uawVipTypes = uawVipTypeMapper.lambdaQuery()
                        .eq(UawVipType::getId, vipMessage.getTypeId())
                        .eq(UawVipType::getVipTypeStatus, SgsConstant.TypeStatus.STARTUSING.getValue())
                        .eq(UawVipType::getStatus, 0).list();
                UawVipType uawVipType = !uawVipTypes.isEmpty() ?uawVipTypes.get(0):null;
                if (uawVipType != null) {
                    homePageVo.setTypeName(uawVipType.getTypeName());
                    homePageVo.setTypeIoc(uawVipType.getIco());
                    homePageVo.setDescription(uawVipType.getDescription());
                    homePageVo.setViptypeId(uawVipType.getId());
                    homePageVo.setCode(uawVipType.getCode());
                    homePageVo.setGroupId(uawVipType.getGroupId());
                    homePageVo.setDescription(uawVipType.getDescription());
                    UawVipTypeGroup uawVipTypeGroup = uawVipTypeGroupMapper.getById(uawVipType.getGroupId());
                    homePageVo.setGroupName(uawVipTypeGroup.getGroupName());
                }
                //工作台
                UawWorkbench uawWorkbench = null;
                if (uawVipType != null) {
                    uawWorkbench = uawWorkbenchMapper.getById(uawVipType.getWorkbench());
                }
                if (uawWorkbench == null) {
                    throw new AeotradeException("工作台出问题了！！！！！请联系技术人员");
                }
                homePageVo.setLoginUrl(uawWorkbench.getLoginUrl());
                homePageVo.setWorkbenchName(uawWorkbench.getWorkbenchName());
                homePageVo.setWorkbenchId(uawWorkbench.getId());
                homePageVo.setWorkbenchIco(uawWorkbench.getIco());
                homePageVo.setChannelColumnsId(uawWorkbench.getChannelColumnsId());
                homePageVo.setWorkbenchDescription(uawWorkbench.getDescription());
                homePageVo.setBanner(uawWorkbench.getBanner());
                homePageVo.setWorkbenchStatus(uawWorkbench.getWorkbenchStatus());
                //会员等级
                List<UawVipClass> uawVipClasses = uawVipClassMapper.lambdaQuery()
                        .eq(UawVipClass::getClassSerialNumber, vipMessage.getClassSerialNumber())
                        .eq(UawVipClass::getStatus, 0).list();
                UawVipClass uawVipClass = !uawVipClasses.isEmpty() ?uawVipClasses.get(0):null;
                if (uawVipClass != null) {
                    homePageVo.setUawVipClass(uawVipClass);
                    //等级权益类型
                    List<UawRightsType> uawRightsTypes = uawRightsTypeMapper.selectJoinList(UawRightsType.class,
                            MPJWrappers.<UawRightsType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                    .selectAll(UawRightsType.class)
                                    .leftJoin(UawVipRightsType.class, UawVipRightsType::getRightsTypeId, UawRightsType::getId)
                                    .eq(UawRightsType::getStatus, 0)
                                    .eq(UawVipRightsType::getStatus, 0)
                                    .eq(UawVipRightsType::getVipClassId, uawVipClass.getId())
                    );
                    homePageVo.setUawRightsTypes(uawRightsTypes);
                }
                homePageVolist.add(homePageVo);
            }
            return homePageVolist;
        } else {
            Boolean aBoolean = this.loginMessage(id, 3);
            if (aBoolean) {
                return findMessage(id, apply);
            }
            throw new AeotradeException("当前企业无法获取工作台信息,请联系工作人员：010-86398171");
        }
    }

    /**
     * @description: 运营端企业入驻申请通过添加会员信息
     * @return:
     * @author: wuhao
     * @date:
     */
    public void insertMessage(Long memberId, Long typeId) throws ParseException {
        List<UawVipMessage> list = this.lambdaQuery().eq(UawVipMessage::getUserType, 1)
                .eq(UawVipMessage::getMemberId, memberId)
                .eq(UawVipMessage::getStatus, 0)
                .eq(UawVipMessage::getTypeId, typeId).list();
        if (null == list || list.isEmpty()) {
            //获取当前时间
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = df.format(new Date());
            UawVipType uawVipType = uawVipTypeMapper.getById(typeId);
            if (uawVipType == null) {
                throw new AeotradeException("会员类型对象为空");
            }
            if (uawVipType.getDefaultVipClassId() == null) {
                throw new AeotradeException("默认等级为空");
            }
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String s = adminFeign.sendToken(memberId, 1, uawVipType.getWorkbench(), "0");
                        System.out.println("调用结果" + s);
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            UawVipClass uawVipClass = uawVipClassMapper.getById(uawVipType.getDefaultVipClassId());
            if (uawVipClass != null) {
                UawVipMessage uawVipMessage = new UawVipMessage();
                uawVipMessage.setMemberId(memberId);
                uawVipMessage.setClassSerialNumber(uawVipClass.getClassSerialNumber());
                uawVipMessage.setVipStatus(1);
                uawVipMessage.setUserType(1);
                uawVipMessage.setTypeId(typeId);
                uawVipMessage.setStartTime(LocalDateTime.now());
                //根据会员等级单位进行结束时间判断
                if (uawVipClass.getTermUnit().equals("年") && uawVipClass.getTerm() != 0) {
                    Date date = DateUlit.dateAddYears(df.parse(format), uawVipClass.getTerm());
                    uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                } else if (uawVipClass.getTermUnit().equals("月") && uawVipClass.getTerm() != 0) {
                    Date date = DateUlit.dateAddMonths(df.parse(format), uawVipClass.getTerm());
                    uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                } else if (uawVipClass.getTermUnit().equals("日") && uawVipClass.getTerm() != 0) {
                    Date date = DateUlit.dateAddDays(df.parse(format), uawVipClass.getTerm());
                    uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                }
                uawVipMessage.setCreatedTime(LocalDateTime.now());
                uawVipMessage.setUpdatedTime(LocalDateTime.now());
                this.saveOrUpdate(uawVipMessage);
                ThreadPoolUtils.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String format = df.format(new Date());
                            UacMember memberById = uacMemberService.getById(memberId);
                            UawVipTypeGroup uawVipTypeGroup = uawVipTypeGroupMapper.getById(uawVipType.getGroupId());
                            String main = "用户已开通:" + uawVipType.getTypeName() + "·" + uawVipClass.getClassName() + "\n" +
                                    "企业名称:" + memberById.getMemberName() + "\n" +
                                    "社会统一信用代码:" + memberById.getUscCode() + "\n" +
                                    "慧贸OS角色:" + uawVipTypeGroup.getGroupName() + "\n" +
                                    "时间:" + format + "\n";
                            SendTextMessage.sendWithAtAll(main, token, secret);
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }
                    }
                });
            }
        }
    }


    @Transactional
    public Boolean loginMessage(Long id, int apply) throws Exception {
        if (apply == 3) {
            Thread.sleep(2000);
            apply = 1;
        } else {
            apply = 0;
        }
        //获取当前时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        List<UawVipTypeGroup> uawVipTypeGroups = uawVipTypeGroupMapper.lambdaQuery()
                .eq(UawVipTypeGroup::getApply, apply)
                .eq(UawVipTypeGroup::getIsDefaultVip, 1)
                .eq(UawVipTypeGroup::getStatus, 0).list();
        UawVipTypeGroup vipTypeGroup = !uawVipTypeGroups.isEmpty() ?uawVipTypeGroups.get(0):null;
        List<UawVipType> uawVipTypes = uawVipTypeMapper.lambdaQuery().eq(UawVipType::getIsDefaultVip, 1)
                .eq(UawVipType::getGroupId, vipTypeGroup.getId())
                .eq(UawVipType::getStatus, 0).list();
        UawVipType vipType = !uawVipTypes.isEmpty() ?uawVipTypes.get(0):null;
        if (vipType == null || vipType.getDefaultVipClassId() == null) {
            return false;
        }
        List<UawVipMessage> list = this.lambdaQuery()
                .eq(UawVipMessage::getUserType, apply)
                .eq(UawVipMessage::getTypeId, vipType.getId())
                .eq(apply == 0 ? UawVipMessage::getStaffId : UawVipMessage::getMemberId, id)
                .eq(UawVipMessage::getStatus, 0).list();
        System.out.println(list.size());
        if (!list.isEmpty()) {
            System.out.println("集合不为空");
            return true;
        }
        UawVipClass uawVipClass = uawVipClassMapper.getById(vipType.getDefaultVipClassId());
        System.out.println("会员等级返回" + uawVipClass);
        UawVipMessage uawVipMessage = new UawVipMessage();
        if (apply == 0) {
            //将用户id添加到对象中
            uawVipMessage.setStaffId(id);
        } else if (apply == 1) {
            uawVipMessage.setMemberId(id);
        }
        uawVipMessage.setClassSerialNumber(uawVipClass.getClassSerialNumber());
        uawVipMessage.setVipStatus(1);
        uawVipMessage.setUserType(apply);
        uawVipMessage.setTypeId(vipType.getId());
        uawVipMessage.setStartTime(LocalDateTime.now());
        uawVipMessage.setCreatedTime(LocalDateTime.now());
        uawVipMessage.setUpdatedTime(LocalDateTime.now());
        uawVipMessage.setStatus(0);
        uawVipMessage.setRevision(0);
        //根据会员等级单位进行结束时间判断
        if (uawVipClass.getTermUnit().equals("年") && uawVipClass.getTerm() != 0) {
            Date date = DateUlit.dateAddYears(df.parse(format), uawVipClass.getTerm());
            uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
        } else if (uawVipClass.getTermUnit().equals("月") && uawVipClass.getTerm() != 0) {
            Date date = DateUlit.dateAddMonths(df.parse(format), uawVipClass.getTerm());
            uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
        } else if (uawVipClass.getTermUnit().equals("日") && uawVipClass.getTerm() != 0) {
            Date date = DateUlit.dateAddDays(df.parse(format), uawVipClass.getTerm());
            uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
        }
        boolean insert = this.save(uawVipMessage);
        return insert;
    }


    public List<UawVipMessage> findUawVipMessage() {
        return this.lambdaQuery().eq(UawVipMessage::getVipStatus, 1)
                .eq(UawVipMessage::getUserType, 1)
                .eq(UawVipMessage::getStatus, 0)
                .isNotNull(UawVipMessage::getEndTime).list();
    }

    //时间叠加
    private LocalDateTime timecompute(LocalDateTime endTime, LocalDateTime endtime) throws ParseException {
        //时间叠加
        long time = endTime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();//当前结束时间

        long Timer = LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli();//当前系统时间
        long timer = endtime.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();//新添加的时间
        Date date = new Date();
        date.setTime(time + (timer - Timer));//当前结束时间+（新添加的时间-当前系统时间）
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    @Transactional
    public void postpone(PostponeTimeVo postponeTimeVo) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = df.format(new Date());
        if (null == postponeTimeVo.getId()) {
            throw new AeotradeException("会员信息id不能为空");
        }
        UawVipMessage uawVipMessage = this.getById(postponeTimeVo.getId());
        if (null == uawVipMessage.getEndTime()) {
            throw new AeotradeException("无限期会员等级无法延期");
        }
        if (uawVipMessage.getVipStatus() == 0) {
            uawVipMessage.setEndTime(LocalDateTime.now());
        }
        Date date = DateUlit.dateAddDays(df.parse(format), postponeTimeVo.getDay());
        uawVipMessage.setEndTime(this.timecompute(uawVipMessage.getEndTime(), LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())));
        uawVipMessage.setVipStatus(1);
        this.saveOrUpdate(uawVipMessage);
        this.insertPostPone(postponeTimeVo);
    }

    public void insertPostPone(PostponeTimeVo postponeTimeVo) {
        UawVipPostpone uawVipPostpone = new UawVipPostpone();
        uawVipPostpone.setClassSerialNumber(postponeTimeVo.getClassSerialNumber());
        uawVipPostpone.setDay(postponeTimeVo.getDay());
        uawVipPostpone.setMamberName(postponeTimeVo.getMamberName());
        uawVipPostpone.setMemberId(postponeTimeVo.getMemberId());
        uawVipPostpone.setMemberName(postponeTimeVo.getMemberName());
        uawVipPostpone.setOperator(postponeTimeVo.getOperator());
        uawVipPostpone.setOperatorId(postponeTimeVo.getOperatorId());
        uawVipPostpone.setOperatorTime(LocalDateTime.now());
        uawVipPostpone.setVipTypeName(postponeTimeVo.getVipTypeName());
        uawVipPostpone.setVipTypeId(postponeTimeVo.getVipTypeId());
        uawVipPostponeMapper.saveOrUpdate(uawVipPostpone);
    }

    public List<UacMember> listMemberPage(String name) {

        List<UacMember> uacMembers = new ArrayList<>();
        if (StringUtils.isNotEmpty(name)) {
            uacMembers = uacMemberService.lambdaQuery()
                    .like(UacMember::getMemberName, name).or().like(UacMember::getUscCode, name)
                    .eq(UacMember::getKindId, 1)
                    .eq(UacMember::getStatus, 0).list();
        } else {
            uacMembers = uacMemberService.lambdaQuery()
                    .eq(UacMember::getKindId, 1)
                    .eq(UacMember::getStatus, 0).list();
        }
        return uacMembers;

    }

    public int findRole(Long platformId, Long memberId, Long organ) {
        int role = 0;
        if (null != organ) {
            role = Math.toIntExact(uacRoleService.lambdaQuery()
                    .eq(UacRole::getPlatformId, platformId)
                    .eq(UacRole::getOrgid, memberId)
                    .eq(UacRole::getIsModel, 0)
                    .eq(UacRole::getOrgan, organ).count());
        } else {
//            role = uawVipMessageMapper.findRole(platformId, memberId);
            role = Math.toIntExact(uacRoleService.lambdaQuery()
                    .eq(UacRole::getPlatformId, platformId)
                    .eq(UacRole::getOrgid, memberId).count());
        }
        if (role > 0) {
            return 1;
        }
        return 0;

    }


    public UacMember findAdmin(Long id, Long memberId) {
        List<UacMember> list = uacMemberService.lambdaQuery().eq(UacMember::getStaffId, id)
                .eq(UacMember::getId, memberId)
                .eq(UacMember::getKindId, 1)
                .eq(UacMember::getStatus, 0).list();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;

    }

    public List<UawWorkbenchMenu> findByStaff(Long id) {
        return uawWorkbenchMenuMapper.selectJoinList(UawWorkbenchMenu.class,
                MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawWorkbenchMenu.class)
                        .leftJoin(UawVipType.class, UawVipType::getWorkbench, UawWorkbenchMenu::getWorkbenchId)
                        .leftJoin(UawVipTypeGroup.class, UawVipTypeGroup::getId, UawVipType::getGroupId)
                        .eq(UawVipTypeGroup::getApply, 0)
                        .eq(UawWorkbenchMenu::getType, 1)
                        .eq(UawWorkbenchMenu::getIsHidden, 0)
        );
    }


    public Boolean openVip(Long id, String memberName, String uscc, Long vipClassId, Long vipTypeId) throws Exception {
        List<UawVipMessage> list = this.lambdaQuery()
                .eq(UawVipMessage::getUserType, 1)
                .eq(UawVipMessage::getMemberId, id)
                .eq(UawVipMessage::getStatus, 0).list();
        if (null != list || !list.isEmpty()) {
            return true;
        }
        UawVipType uawVipType = uawVipTypeMapper.getById(vipTypeId);
        UawVipClass uawVipClass = uawVipClassMapper.getById(vipClassId);
        UawAptitudes uawAptitudes = new UawAptitudes();
        uawAptitudes.setVipTypeId(uawVipType.getId());
        uawAptitudes.setMemberId(id);
        uawAptitudes.setCreatedBy("系统");
        uawAptitudes.setCreatedTime(LocalDateTime.now());
        if (null != memberName) {
            uawAptitudes.setMemberName(memberName);
        }
        if (null != uscc) {
            uawAptitudes.setUscc(uscc);
        }
        uawAptitudes.setVipGroupName("服务商ERP");
        uawAptitudes.setVipTypeName(uawVipType.getTypeName());
        uawAptitudes.setAuditor("系统");
        uawAptitudesService.sgsListSave(uawAptitudes);
        UawVipMessage vipMessage = new UawVipMessage();
        vipMessage.setUserType(1);
        vipMessage.setMemberId(id);
        if (vipTypeId != null) {
            vipMessage.setTypeId(vipTypeId);
        }
        List<UawVipMessage> listTwo = this.lambdaQuery(vipMessage).list();
        if (null != listTwo && listTwo.size() == 1) {
            for (UawVipMessage uawVipMessage : listTwo) {
                uawVipMessage.setClassSerialNumber(uawVipClass.getClassSerialNumber());
                this.updateById(uawVipMessage);
            }
            return true;
        }
        return false;
    }




    public String vipUpdate(MessageUpdate messageUpdate) throws Exception {
        List<UawVipMessage> messagelist = this.lambdaQuery()
                .eq(UawVipMessage::getMemberId, messageUpdate.getMemberId())
                .eq(UawVipMessage::getTypeId, messageUpdate.getNewVipTypeId())
                .eq(UawVipMessage::getStatus, 0).list();
        List<UawVipClass> vipClasses = uawVipClassMapper.lambdaQuery()
                .eq(UawVipClass::getClassSerialNumber, messageUpdate.getNewVipClass())
                .eq(UawVipClass::getStatus, 0).list();
        UawVipClass vipClass = !vipClasses.isEmpty() ?vipClasses.get(0):null;
        if (null != messagelist && !messagelist.isEmpty()) {
            for (UawVipMessage uawVipMessage : messagelist) {
                if (null != uawVipMessage.getEndTime() && vipClass.getTerm() != 0) {
                    uawVipMessage.setClassSerialNumber(messageUpdate.getNewVipClass());
                    this.updateById(uawVipMessage);
                } else if (null != uawVipMessage.getEndTime() && vipClass.getTerm() == 0) {
                    this.removeById(uawVipMessage);
                    uawVipMessage.setClassSerialNumber(messageUpdate.getNewVipClass());
                    uawVipMessage.setEndTime(null);
                    this.save(uawVipMessage);
                } else if (null == uawVipMessage.getEndTime() && vipClass.getTerm() == 0) {
                    uawVipMessage.setClassSerialNumber(messageUpdate.getNewVipClass());
                    this.updateById(uawVipMessage);
                } else {
                    //获取当前时间
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String format = df.format(new Date());
                    uawVipMessage.setClassSerialNumber(messageUpdate.getNewVipClass());
                    //根据会员等级单位进行结束时间判断
                    if (vipClass.getTermUnit().equals("年") && vipClass.getTerm() != 0) {
                        Date date = DateUlit.dateAddYears(df.parse(format), vipClass.getTerm());
                        uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                    } else if (vipClass.getTermUnit().equals("月") && vipClass.getTerm() != 0) {
                        Date date = DateUlit.dateAddMonths(df.parse(format), vipClass.getTerm());
                        uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                    } else if (vipClass.getTermUnit().equals("日") && vipClass.getTerm() != 0) {
                        Date date = DateUlit.dateAddDays(df.parse(format), vipClass.getTerm());
                        uawVipMessage.setEndTime(LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault()));
                    }
                    this.updateById(uawVipMessage);
                }
            }
            return "ok";
        } else {
            List<UawVipMessage> list = this.lambdaQuery()
                    .eq(UawVipMessage::getMemberId, messageUpdate.getMemberId())
                    .eq(UawVipMessage::getTypeId, messageUpdate.getOldVipTypeId())
                    .eq(UawVipMessage::getStatus, 0).list();
            if (null != list && !list.isEmpty()) {
                UacMember memberId = uacMemberService.getById(messageUpdate.getMemberId());
                if (null == memberId) {
                    return "企业不存在,请联系技术人员";
                }
                UawAptitudes uawAptitudes = new UawAptitudes();
                uawAptitudes.setMemberId(messageUpdate.getMemberId());
                uawAptitudes.setMemberName(memberId.getMemberName());
                uawAptitudes.setUscc(memberId.getUscCode());
                uawAptitudes.setCreatedById(memberId.getStaffId());
                uawAptitudes.setVipTypeId(messageUpdate.getNewVipTypeId());
                uawAptitudes.setSgsStatus(1);
                UawAptitudes uawAptitudes1 = uawAptitudesService.sgsListSave(uawAptitudes);
                List<UawVipMessage> list1 = this.lambdaQuery()
                        .eq(UawVipMessage::getMemberId, messageUpdate.getMemberId())
                        .eq(UawVipMessage::getTypeId, messageUpdate.getNewVipTypeId())
                        .eq(UawVipMessage::getStatus, 0).list();
                if (null == list1 || list1.size() == 0) {
                    return "修改失败，请联系技术人员";
                }
                for (UawVipMessage message : list1) {

                    if (null != list.get(0).getEndTime() && vipClass.getTerm() != 0) {
                        message.setEndTime(list.get(0).getEndTime());
                    }
                    message.setClassSerialNumber(messageUpdate.getNewVipClass());
                    this.updateById(message);
                }
                for (UawVipMessage message : list) {
                    this.removeById(message);
                }
                List<UawAptitudes> aptitudesServiceList = uawAptitudesService.lambdaQuery()
                        .eq(UawAptitudes::getMemberId, messageUpdate.getMemberId())
                        .eq(UawAptitudes::getVipTypeId, messageUpdate.getOldVipTypeId())
                        .eq(UawAptitudes::getStatus, 0).list();
                for (UawAptitudes uawAptitudes2 : aptitudesServiceList) {
                    uawAptitudes2.setStatus(1);
                    uawAptitudesService.updateById(uawAptitudes2);
                }
                return "ok";
            } else {
                return "数据错误，请联系技术人员";
            }
        }
    }

    public Long findMemberPower(Long memberId, String url) {
        List<String> collect = this.lambdaQuery().eq(UawVipMessage::getMemberId, memberId)
                .eq(UawVipMessage::getVipStatus, 1)
                .eq(UawVipMessage::getUserType, 1)
                .eq(UawVipMessage::getStatus, 0).list()
                .stream().map(UawVipMessage::getClassSerialNumber).collect(Collectors.toList());
        List<Long> classLists = uawVipClassMapper.lambdaQuery()
                .in(UawVipClass::getClassSerialNumber, collect)
                .list()
                .stream().map(UawVipClass::getId).collect(Collectors.toList());
        List<Long> workbenchLists = uawWorkbenchMenuMapper.lambdaQuery()
                .eq(UawWorkbenchMenu::getStatus, 0)
                .eq(UawWorkbenchMenu::getType, 2)
                .eq(UawWorkbenchMenu::getPlatformType, 3)
                .eq(UawWorkbenchMenu::getButtonPath, url).list()
                .stream().map(UawWorkbenchMenu::getId).collect(Collectors.toList());
        return uawVipClassMenuMapper.lambdaQuery()
                .in(UawVipClassMenu::getMenuId, workbenchLists)
                .in(UawVipClassMenu::getClassId, classLists).count();
    }
}

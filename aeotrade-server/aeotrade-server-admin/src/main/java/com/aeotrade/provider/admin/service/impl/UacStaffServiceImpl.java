package com.aeotrade.provider.admin.service.impl;



import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.service.*;
import com.aeotrade.provider.admin.uacVo.*;

import com.aeotrade.provider.admin.mapper.UacStaffMapper;

import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 企业员工 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class UacStaffServiceImpl extends ServiceImpl<UacStaffMapper, UacStaff> implements UacStaffService {

    @Autowired
    private UacMemberServiceImpl uacMemberService;
    @Autowired
    private UacUserConnectionServiceImpl uacUserConnectionService;
    @Autowired
    private UacMemberStaffServiceImpl uacMemberStaffMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private UacUserConnectionServiceImpl uacUserConnectionMapper;
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacAdminRoleService uacAdminRoleService;
    @Autowired
    private UacRoleService uacRoleService;
    @Autowired
    private UacDeptService uacDeptService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;
    @Value("${hmtx.bcChainUri:}")
    private String BcChainUri;

    public UacStaff saveUacStaff(UacStaff staff) {
        staff.setCreatedTime(LocalDateTime.now());
        staff.setUpdatedTime(LocalDateTime.now());
        staff.setId(null);
        staff.setStatus(0);
        staff.setSgsStatus(1);
        return staff;
    }

    public UacStaffDto saveMemberAndStaff(UacStaffDto uacStaffDto) {

        //保存企业数据
        UacMember uacMember = new UacMember();
        uacMember.setMemberName(uacStaffDto.getMemberName());
        uacMember.setStasfTel(uacStaffDto.getStasfTel());
        uacMember.setKindId(uacStaffDto.getKindId());
        uacMember.setAtpwStatus(0);
        //  uacMember.setPersonageStatus(0);
        uacMember.setEmail(uacStaffDto.getEmail());
        uacMember.setStaffId(uacStaffDto.getStaffId());
        uacMemberService.save(uacMember);
        uacStaffDto.setMemberId(uacMember.getId());

        //保存操作人数据
        UacStaff staff = new UacStaff();
        staff.setTel(uacStaffDto.getStasfTel());
        staff.setMemberId(uacMember.getId());
        staff.setStaffName(uacStaffDto.getMemberName());
        staff.setWxOpenid(uacStaffDto.getWxOpenid());
        staff.setWxLogo(uacStaffDto.getWxLogo());
        staff.setWxUnionid(uacStaffDto.getUnionid());
        staff.setStaffType(BizConstant.StaffTypeEnum.ENTERPRISE.getValue());
        save(staff);
        uacStaffDto.setStaffId(staff.getId());

        //关联WECHAT数据
//        List<UacUserConnection> uacUserConnectionList = uacUserConnectionService.findByWechatUserId(uacStaffDto.getUnionid());
        List<UacUserConnection> uacUserConnectionList = uacUserConnectionService.lambdaQuery()
                .eq(UacUserConnection::getUnionid, uacStaffDto.getUnionid()).list();
        if (uacUserConnectionList.size() != 1) {
            throw new AeotradeException("请扫码登录后再绑定企业用户数据");
        }
        UacUserConnection uacUserConnection = uacUserConnectionList.get(0);
        uacUserConnection.setStaffId(staff.getId());
        uacUserConnectionService.updateById(uacUserConnection);

        return uacStaffDto;
    }

    /**
     * 根据员工id删除员工
     *
     * @param memberId
     */
    public void deleteStaff(Long memberId, Long staffId,String token) throws Exception {
        List<UacMemberStaff> uacMemberStaffs = uacMemberStaffMapper.lambdaQuery()
                .eq(UacMemberStaff::getMemberId, memberId)
                .eq(UacMemberStaff::getStaffId, staffId).list();
        for (UacMemberStaff uacMemberStaff : uacMemberStaffs) {
            uacMemberStaffMapper.removeById(uacMemberStaff);
        }

        List<Long> adminlongList = uacAdminService.lambdaQuery()
                .eq(UacAdmin::getStaffId, staffId)
                .list().stream().map(UacAdmin::getId).collect(Collectors.toList());
        if(adminlongList.size()!=0){
            List<Long> rolelongList = uacRoleService.lambdaQuery()
                    .eq(UacRole::getOrgid, memberId).list().stream().map(UacRole::getId).collect(Collectors.toList());
            if(rolelongList.size()!=0){
                List<UacAdminRole> adminRoles = uacAdminRoleService.lambdaQuery()
                        .in(UacAdminRole::getRoleId, rolelongList)
                        .eq(UacAdminRole::getMemberId, memberId)
                        .in(UacAdminRole::getAdminId, adminlongList).list();
                for (UacAdminRole adminRole : adminRoles) {
                    uacAdminRoleService.removeById(adminRole);
                }
            }
        }
        MPJLambdaWrapper<UacDeptStaff> uacDeptStaffMPJLambdaWrapper = MPJWrappers.<UacDeptStaff>lambdaJoin().disableSubLogicDel().disableLogicDel();
        uacDeptStaffMPJLambdaWrapper.selectAll(UacDeptStaff.class)
                .leftJoin(UacDept.class,UacDept::getId,UacDeptStaff::getDeptId)
                .eq(UacDept::getMemberId,memberId)
                .eq(UacDeptStaff::getStaffId,staffId);
        List<UacDeptStaff> uacDeptStaffs = uacDeptStaffService.selectJoinList(UacDeptStaff.class, uacDeptStaffMPJLambdaWrapper);
        if(uacDeptStaffs.size()!=0){
            uacDeptStaffService.removeBatchByIds(uacDeptStaffs);
        }
        /**1.根据staffId删除关联表信息*/
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> map = new HashMap<>();
                    map.put("memberId", memberId);
                    map.put("staffId", staffId);
                    if (!org.springframework.util.StringUtils.isEmpty(BcChainUri)) {
                        String http = HttpRequestUtils.httpGet(BcChainUri, token, map, "utf-8");
                    }
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });

    }


    /**
     * 添加员工
     *
     * @param staffDto
     * @return
     */
    @Transactional
    public Long insertStaff(StaffDto staffDto) {
        //如果员工id不为null 修改 否则添加
        if (staffDto.getIds().length != 0) {
            staffUpdate(staffDto);

            return staffDto.getMember().get(0).getId();
        } else {

            /**1.添加员工表*/
            UacStaff uacStaff = new UacStaff();
            if (staffDto.getMember().size() != 0) {

                List<MemberVO> member = staffDto.getMember();
                MemberVO memberVO = member.get(0);

                if (staffDto != null) {
                    BeanUtils.copyProperties(staffDto, uacStaff);
                    uacStaff.setId(null);
                    uacStaff.setCreatedTime(LocalDateTime.now());
                    uacStaff.setMemberId(memberVO.getId());
                    uacStaff.setSgsStatus(BizConstant.StaffBindStateEum.UNBOUND.getValue());
                    this.save(uacStaff);

                    /**2.添加关联表*/
                    UacMemberStaff uacMemberStaff = null;
                    for (MemberVO mvo : member) {
                        uacMemberStaff = new UacMemberStaff();
                        uacMemberStaff.setStaffId(uacStaff.getId());
                        uacMemberStaff.setMemberId(mvo.getId());
                        uacMemberStaff.setIsAdmin(0);
                        uacMemberStaff.setCreatedTime(LocalDateTime.now());
                        uacMemberStaffMapper.save(uacMemberStaff);
                    }
                    return uacStaff.getId();
                }
            }
        }
        return null;
    }

    /**
     * 编辑员工信息
     *
     * @param staffDto
     */
    public void updateStaff(StaffDto staffDto) {
        if (staffDto.getIds().length != 0) {
            //企业管理批量修改员工
            staffUpdate(staffDto);
        } else {
            //企业管理编辑
            memberUpdate(staffDto);
        }
    }

    /**
     * 员工管理编辑
     *
     * @param staffDto
     */
    private void staffUpdate(StaffDto staffDto) {
        Long[] ids = staffDto.getIds();
        for (Long id : ids) {
            /**1.删除关联表数据从新添加*/
            /*1.1根据员工id查询关联表数据*/
            List<UacMemberStaff> staff = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, id).list();
            UacMemberStaff me = null;
            for (UacMemberStaff uac : staff) {
                me = new UacMemberStaff();
                if (uac.getId() != null) {
                    me.setId(uac.getId());
                    uacMemberStaffMapper.removeById(me);
                }
            }
            /*1.2添加关联表数据*/
            List<MemberVO> member = staffDto.getMember();
            for (MemberVO vo : member) {
                me = new UacMemberStaff();
                me.setStaffId(id);
                me.setMemberId(vo.getId());
                me.setIsAdmin(0);
                me.setCreatedTime(LocalDateTime.now());
                uacMemberStaffMapper.save(me);
            }
        }

    }

    /**
     * 企业管理编辑
     *
     * @param staffDto
     */
    private void memberUpdate(StaffDto staffDto) {
        UacStaff uacStaff = new UacStaff();
        if (staffDto.getId() != null) {
            BeanUtils.copyProperties(staffDto, uacStaff);
            uacStaff.setUpdatedTime(LocalDateTime.now());
            uacStaff.setRevision(1);
            uacStaff.setMemberId(staffDto.getMember().get(0).getId());
            this.updateById(uacStaff);
        }
        if (staffDto.getMember().size() != 0) {
            List<MemberVO> member = staffDto.getMember();
            for (MemberVO id : member) {
                List<UacMemberStaff> staff = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, id).list();
                UacMemberStaff me = null;
                for (UacMemberStaff uac : staff) {
                    me = new UacMemberStaff();
                    if (uac.getId() != null) {
                        me.setId(uac.getId());
                        uacMemberStaffMapper.removeById(me);
                    }
                }
                /*1.2添加关联表数据*/
                List<MemberVO> members = staffDto.getMember();
                for (MemberVO vo : member) {
                    me = new UacMemberStaff();
                    me.setStaffId(id.getId());
                    me.setMemberId(vo.getId());
                    me.setIsAdmin(0);
                    me.setCreatedTime(LocalDateTime.now());
                    uacMemberStaffMapper.save(me);
                }
            }
        }
    }

    public MemberStaffVO findStaffById(Long id) {
        /**1.根据id查询员工信息*/
        UacStaff uacStaff = this.getById(id);
        if (uacStaff != null) {
            MemberStaffVO vo = new MemberStaffVO();
            BeanUtils.copyProperties(uacStaff, vo);
            /**2.根据id查询中间表企业id*/
            List<UacMemberStaff> staff = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, id).list();

            List<MemberVO> member = new ArrayList<>();
            MemberVO memberVO = null;
            for (UacMemberStaff sta : staff) {
                /**3.根据企业id查询企业信息*/
                if (sta.getMemberId() != null) {
                    memberVO = new MemberVO();
                    UacMember uacMember = uacMemberMapper.get(sta.getMemberId());
                    if (uacMember != null) {
                        BeanUtils.copyProperties(uacMember, memberVO);
                        member.add(memberVO);
                    }
                }
            }
            vo.setMember(member);
            return vo;
        }
        return null;
    }

    /**
     * 切换企业
     *
     * @param id
     * @param memberId
     */
    public void switchMember(Long id, Long memberId) {
        UacStaff uac = new UacStaff();
        uac.setId(id);
        uac.setMemberId(memberId);
        this.updateById(uac);
    }

    public MemberStaffTelVO findStaffByIdH5(Long staffId, Long inviteId) {
        /**1.根据id查询员工信息*/
        UacStaff uacStaff = this.getById(inviteId);
        if (uacStaff != null) {
            MemberStaffTelVO vo = new MemberStaffTelVO();
            BeanUtils.copyProperties(uacStaff, vo);
            /**2.根据id查询中间表企业id*/
            List<UacMemberStaff> staff = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getStaffId, inviteId).list();

            List<MemberVO> member = new ArrayList<>();
            MemberVO memberVO = null;
            for (UacMemberStaff sta : staff) {
                /**3.根据企业id查询企业信息*/
                if (sta.getMemberId() != null) {
                    memberVO = new MemberVO();
                    UacMember uacMember = uacMemberMapper.get(sta.getMemberId());
                    if (uacMember != null) {
                        BeanUtils.copyProperties(uacMember, memberVO);
                        member.add(memberVO);
                    }
                }
            }

            UacStaff um = this.getById(staffId);
            if (um != null && StringUtils.isNotEmpty(um.getStaffName())) {
                vo.setSedMemberName(um.getStaffName());
            }
            vo.setMember(member);
            return vo;
        }
        return null;
    }

    /**
     * 手机端拒绝接受邀请
     *
     * @param id
     */
    public void rejectInvite(Long id) throws RuntimeException {
        UacStaff uacStaff = this.getById(id);
        if (uacStaff != null && uacStaff.getSgsStatus() == BizConstant.StaffBindStateEum.BINDING_FAILED.getValue()) {
            throw new AeotradeException("请不要重复拒绝");
        }
        UacStaff uac = new UacStaff();
        uac.setId(id);
        uac.setSgsStatus(BizConstant.StaffBindStateEum.BINDING_FAILED.getValue());
        this.updateById(uac);
    }

    /**
     * 接受用户邀请
     *
     * @param wxTokenDto
     * @param uacStaff
     */
    @Transactional
    public void saveUser(WxTokenDto wxTokenDto, UacStaff uacStaff) {
        UacUserConnection userConnection = new UacUserConnection();
        userConnection.setProviderUserId(wxTokenDto.getOpenid());
        userConnection.setProviderId(BizConstant.ClientEnum.WECHAT.toString().toLowerCase());
        userConnection.setDisplayName(wxTokenDto.getNickname());
        userConnection.setImageUrl(wxTokenDto.getHeadimgurl());
        userConnection.setUnionid(wxTokenDto.getUnionid());
        userConnection.setAccessToken(wxTokenDto.getAccess_token());
        userConnection.setRefreshToken(wxTokenDto.getRefresh_token());
        userConnection.setStaffId(wxTokenDto.getId());
        uacUserConnectionMapper.save(userConnection);
        UacStaff staff = new UacStaff();
        BeanUtils.copyProperties(uacStaff, staff);
        staff.setId(wxTokenDto.getId());
        staff.setSgsStatus(BizConstant.StaffBindStateEum.BOUND.getValue());
        this.updateById(staff);

    }

    public List<SubAdminDto> subAdminList(Long staffId) {
        UacStaff uacStaff = this.getById(staffId);
        List<UacMemberStaff> uacMemberStaffList = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getMemberId, uacStaff.getMemberId()).list();
        Set<Long> longSet = uacMemberStaffList.stream().map(UacMemberStaff::getStaffId).collect(Collectors.toSet());
        List<UacStaff> query = this.lambdaQuery().in(UacStaff::getId, longSet).list();

        return query.stream().map(o -> {
            if (o.getId().longValue() == staffId.longValue())
                return null;
            SubAdminDto subAdminDto = new SubAdminDto();
            subAdminDto.setStaffId(o.getId());
            subAdminDto.setImageUrl(o.getWxLogo());
            subAdminDto.setDisplayName(o.getStaffName());
            return subAdminDto;

        }).collect(Collectors.toList());
    }

    public Map<String, Object> updateStaffById(UacStaff uacStaff) {
        uacStaff.setRevision(1);
        this.updateById(uacStaff);
        Map<String, Object> memberDetails = findMemberDetails(uacStaff.getId());
        return memberDetails;
    }

    private Map<String, Object> findMemberDetails(Long staffId) {
        UacStaff uacStaff = this.getById(staffId);
        UacMember uacMember = uacMemberMapper.get(uacStaff.getMemberId());
        if (uacStaff != null && uacMember != null) {
            Map<String, Object> map = new HashMap<>();
            map.put("memberid", uacStaff.getMemberId().toString());
            map.put("staffid", uacStaff.getId().toString());
            map.put("staffname", uacStaff.getStaffName());
            map.put("kindid", uacMember.getKindId());
            map.put("isatff", 0);
            map.put("uscc", uacMember.getUscCode());
            map.put("membername", uacMember.getMemberName());
            map.put("memberimg", uacMember.getLogoImg());
            map.put("sgsStatus", uacMember.getSgsStatus());
            map.put("memberstatus", uacMember.getMemberStatus());
            map.put("stasfTel", uacMember.getStasfTel());//电话
            map.put("email", uacMember.getEmail());//邮箱
            return map;
        }
        return null;
    }

    @Transactional
    public void delSubAdmin(Long memberId, Long staffId) {
        List<UacMemberStaff> uacMemberStaffList = uacMemberStaffMapper.lambdaQuery()
                .eq(UacMemberStaff::getMemberId, memberId).eq(UacMemberStaff::getStaffId, staffId).list();
        if (uacMemberStaffList.size() > 0) {
            uacMemberStaffList.forEach(uacMemberStaff -> {
                uacMemberStaffMapper.removeById(uacMemberStaff.getId());
            });
        }
    }


    public PageList<UacStaff> findStaffList(Long memberId, Integer pageSize, Integer pageNo, Long workbenchId, Long organ, Long deptId, Long roleId, String staffName) {
        PageList<UacStaff> list = new PageList<>();
        Page<UacMemberStaff> uacMemberStaffs = new Page<>();
        UacMember uacMember = uacMemberMapper.get(memberId);
        UacStaff uacStaff = this.getById(uacMember.getStaffId());
        if (!CommonUtil.isEmpty(uacMember)) {
            List<UacMemberStaff> memberStaffs = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).eq(UacMemberStaff::getStaffId, uacMember.getStaffId()).list();
            UacMemberStaff uacMemberStaff = memberStaffs.size() > 0 ? memberStaffs.get(0) : new UacMemberStaff();
            uacMemberStaff.setMemberId(memberId);
            uacMemberStaff.setStaffId(uacMember.getStaffId());
            uacMemberStaff.setIsAdmin(1);
            uacMemberStaff.setCreatedTime(uacMember.getCreatedTime());
            uacMemberStaff.setKindId(1L);
            uacMemberStaffMapper.saveOrUpdate(uacMemberStaff);

            MPJLambdaWrapper<UacMemberStaff> uacMemberStaffMPJLambdaWrapper = MPJWrappers.<UacMemberStaff>lambdaJoin().disableSubLogicDel().disableLogicDel();
            uacMemberStaffMPJLambdaWrapper.select(UacMemberStaff::getStaffId);
            uacMemberStaffMPJLambdaWrapper.leftJoin(UacMember.class, UacMember::getId, UacMemberStaff::getMemberId);
            uacMemberStaffMPJLambdaWrapper.leftJoin(UacStaff.class, UacStaff::getId, UacMemberStaff::getStaffId);
            uacMemberStaffMPJLambdaWrapper.eq(UacMemberStaff::getMemberId, memberId);
            if (null != deptId) {
                List<Long> depts = uacDeptService.findListById(deptId);
                if(depts.size()==0){
                    return list;
                }
                uacMemberStaffMPJLambdaWrapper.in(UacMemberStaff::getStaffId, depts);
            }
            if (StringUtils.isNotEmpty(staffName)) {
                uacMemberStaffMPJLambdaWrapper.like(UacStaff::getStaffName, staffName);
            }
            if (null != roleId) {
                List<Long> staffIds = uacRoleService.findStaffIds(roleId, memberId);
                if(staffIds.size()==0){
                    return list;
                }
                uacMemberStaffMPJLambdaWrapper.in(UacMemberStaff::getStaffId, staffIds);
            }
            uacMemberStaffMPJLambdaWrapper.orderByDesc(UacMemberStaff::getIsAdmin);
            uacMemberStaffMPJLambdaWrapper.orderByAsc(UacMemberStaff::getCreatedTime);
            uacMemberStaffs = uacMemberStaffMapper.selectJoinListPage(new Page<>(pageNo, pageSize), UacMemberStaff.class, uacMemberStaffMPJLambdaWrapper);
        }
        for (UacMemberStaff uac : uacMemberStaffs.getRecords()) {
            if (uac.getStaffId() != null) {
                UacStaff uacStafftwo = this.getById(uac.getStaffId());
                if (null == uacStafftwo) {
                    continue;
                }
                if (uacStafftwo.getId().equals(uacStaff.getId())) {
                    uacStaff.setRole("主管理员");
                    uacStaff.setCreatedTime(uacStaff.getCreatedTime());
                    list.add(uacStaff);
                    list.set(0, uacStaff);
                } else {
                    if (!uacMember.getStaffId().equals(uac.getStaffId())) {
                        List<String> role = null;
                        if (null != organ) {
                            role = uacRoleService.selectJoinList(UacRole.class,
                                    MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                            .selectAll(UacRole.class)
                                            .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                                            .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                                            .eq(UacAdmin::getStaffId, uacStafftwo.getId())
                                            .eq(UacRole::getOrgan, organ)
                                            .eq(UacAdminRole::getMemberId, uacMember.getId())
                            ).stream().map(UacRole::getName).collect(Collectors.toList());
                        } else {
                            role = uacRoleService.selectJoinList(UacRole.class,
                                    MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                            .selectAll(UacRole.class)
                                            .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                                            .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                                            .eq(UacAdmin::getStaffId, uacStafftwo.getId())
                                            .eq(UacAdminRole::getMemberId, uacMember.getId())
                                            .eq(UacAdminRole::getOrgi, workbenchId)
                            ).stream().map(UacRole::getName).collect(Collectors.toList());
                        }
                        uacStafftwo.setRole(JSON.toJSONString(role));
                        list.add(uacStafftwo);
                    }
                }

            }
        }
        list.setTotalSize(Math.toIntExact(uacMemberStaffs.getTotal()));
        return list;
    }

    public PageList<UacStaff> findAllPage(Integer pageSize, Integer pageNo, String vlaue, String sourceMark) {
        if (StringUtils.isEmpty(vlaue) && StringUtils.isEmpty(sourceMark)) {
            PageList<UacStaff> pageList = new PageList<>();
            LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
            uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
            uacStaffLambdaQueryWrapper.orderByDesc(UacStaff::getCreatedTime);
            Page<UacStaff> list = this.page(new Page<>(pageNo, pageSize), uacStaffLambdaQueryWrapper);
            pageList.setRecords(list.getRecords());
            pageList.setTotalSize(list.getTotal());
            return pageList;
        } else if (StringUtils.isNotEmpty(vlaue) && StringUtils.isEmpty(sourceMark)) {

            PageList<UacStaff> pageList = new PageList<>();
            LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
            uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
            uacStaffLambdaQueryWrapper.like(UacStaff::getTel, vlaue).or().like(UacStaff::getStaffName, vlaue);
            uacStaffLambdaQueryWrapper.orderByDesc(UacStaff::getCreatedTime);
            Page<UacStaff> list = this.page(new Page<>(pageNo, pageSize), uacStaffLambdaQueryWrapper);
            pageList.setRecords(list.getRecords());
            pageList.setTotalSize(list.getTotal());
            return pageList;
        } else {
            PageList<UacStaff> pageList = new PageList<>();
            LambdaQueryWrapper<UacStaff> uacStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
            uacStaffLambdaQueryWrapper.eq(UacStaff::getStatus, 0);
            uacStaffLambdaQueryWrapper.eq(UacStaff::getSourceMark, sourceMark);
            uacStaffLambdaQueryWrapper.like(UacStaff::getTel, vlaue).or().like(UacStaff::getStaffName, vlaue);
            uacStaffLambdaQueryWrapper.orderByDesc(UacStaff::getCreatedTime);
            Page<UacStaff> list = this.page(new Page<>(pageNo, pageSize), uacStaffLambdaQueryWrapper);

            pageList.setRecords(list.getRecords());
            pageList.setTotalSize(list.getTotal());
            return pageList;
        }
    }

    public int quitMember(Long memberId, Long staffId) {
        UacMember uacMember = uacMemberMapper.get(memberId);
        if (uacMember != null && uacMember.getStaffId().equals(staffId)) {
            List<UacMemberStaff> uacMemberStaffs = uacMemberStaffMapper.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).list();
            if (uacMemberStaffs.size() == 0 || (uacMemberStaffs.size() == 1 && uacMemberStaffs.get(0).getStaffId().equals(staffId))) {
                return 1;
            }
            return 2;
        }
        return 3;
    }

    public Map<String, Object> findStaffInfo(Long staffId, Long memberId, Long workbenchId) {
        Map<String, Object> map = new HashMap<>();
        map.put("staffName", StringUtils.EMPTY);
        map.put("tel", StringUtils.EMPTY);
        map.put("email", StringUtils.EMPTY);
        UacStaff byId = this.getById(staffId);
        List<Long> roleId = uacRoleService.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRole.class)
                        .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                        .eq(UacAdmin::getStaffId, staffId)
                        .eq(UacAdminRole::getMemberId, memberId)
                        .eq(UacAdminRole::getOrgi, workbenchId)
        ).stream().map(UacRole::getId).collect(Collectors.toList());

        List<Long> deptIds = uacDeptService.selectJoinList(UacDept.class,
                MPJWrappers.<UacDept>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacDept.class)
                        .leftJoin(UacDeptStaff.class, UacDeptStaff::getDeptId, UacDept::getId)
                        .eq(UacDeptStaff::getStaffId, staffId)
                        .eq(UacDept::getMemberId, memberId)
        ).stream().map(UacDept::getId).collect(Collectors.toList());
        if (StringUtils.isNotEmpty(byId.getStaffName())) {
            map.put("staffName", byId.getStaffName());
        }
        if (StringUtils.isNotEmpty(byId.getTel())) {
            map.put("tel", byId.getTel());
        }
        if (StringUtils.isNotEmpty(byId.getContactEmail())) {
            map.put("email", byId.getContactEmail());
        }
        map.put("roleId", roleId);
        map.put("deptId", deptIds);
        return map;
    }
}

package com.aeotrade.provider.admin.service.impl;



import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.base.model.ChainApplyCredential;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.event.MemberCertAuthenticationEvent;
import com.aeotrade.provider.admin.mapper.SgsConfigurationMapper;
import com.aeotrade.provider.admin.mapper.UacStaffMapper;
import com.aeotrade.provider.admin.uacVo.*;

import com.aeotrade.provider.admin.mapper.SgsCertInfoMapper;
import com.aeotrade.provider.admin.service.SgsCertInfoService;

import com.aeotrade.service.MqSend;
import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业认证 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class SgsCertInfoServiceImpl extends ServiceImpl<SgsCertInfoMapper, SgsCertInfo> implements SgsCertInfoService {
    @Autowired
    private SgsSwInfoServiceImpl sgsSwInfoMapper;
    @Autowired
    private SgsApplyServiceImpl sgsApplyMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private SgsBankInfoServiceImpl sgsBankMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Value("${aeotrade.ops-mail-recipients:}")
    private String recipients;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private SgsConfigurationMapper sgsConfigurationMapper;
    @Autowired
    private UacStaffMapper uacStaffMapper;

    public SgsCertInfo sgsBankSave(SgsCertInfo sgsCertInfo) {
        SgsApply sgsInfo = new SgsApply();
        sgsInfo.setMemberId(sgsCertInfo.getMemberId());
        sgsInfo.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
        sgsInfo.setSgsType(SgsConstant.SgsType.MEMBER_ID.getValue());
        sgsInfo.setSgsStatus(SgsConstant.SgsStatus.WEIRENZ.getValue());
        sgsInfo.setSgsTypeName("企业证件认证");
        sgsInfo.setMemberName(sgsCertInfo.getMemberName());
        sgsInfo.setUscc(sgsCertInfo.getUscc());
        sgsInfo.setStatus(0);
        sgsInfo.setCreatedTime(LocalDateTime.now());
        sgsInfo.setUpdatedTime(LocalDateTime.now());
        sgsApplyMapper.save(sgsInfo);
        sgsCertInfo.setSgsApplyId(sgsInfo.getId());
        sgsCertInfo.setStatus(0);
        sgsCertInfo.setSgsStatus(SgsConstant.SgsStatus.WEIRENZ.getValue());
        this.save(sgsCertInfo);
        if (!org.springframework.util.StringUtils.isEmpty(recipients)) {
            String recipient = recipients;
            String[] serndEmial = recipient.split(",");
            /**2.发送邮件*/
            MemberCertAuthenticationEvent memberCertAuthenticationEvent=new MemberCertAuthenticationEvent(this,sgsCertInfo,serndEmial);
            eventPublisher.publishEvent(memberCertAuthenticationEvent);
        }
        return sgsCertInfo;
    }

    public SgsMemberVO memberSgsQuery(Long id) {
        SgsMemberVO vo = new SgsMemberVO();
        UacMember uacMember = uacMemberMapper.getById(id);
        if (uacMember != null) {
            vo.setUacMember(uacMember);
        }
        List<SgsCertInfo> list = this.lambdaQuery().eq(SgsCertInfo::getMemberId, id).list();
        if (!CommonUtil.isEmpty(list)) {
            vo.setSgsMember(list.get(0));
        }
        return vo;
    }

    public PageList<SgsApply> memberSgsLisst(Integer pageSize, Integer pageNo, String value, int status) {
        PageList<SgsApply> volist = new PageList<>();
        LambdaQueryWrapper<SgsApply> sgsApplyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sgsApplyLambdaQueryWrapper.eq(SgsApply::getSgsStatus, status);
        sgsApplyLambdaQueryWrapper.eq(SgsApply::getStatus, 0);
        sgsApplyLambdaQueryWrapper.like(SgsApply::getUscc, value).or().like(SgsApply::getMemberName, value);
        sgsApplyLambdaQueryWrapper.orderByDesc(SgsApply::getUpdatedTime);
        Page<SgsApply> page = sgsApplyMapper.page(new Page<>(pageNo, pageSize), sgsApplyLambdaQueryWrapper);
        List<SgsApply> records = page.getRecords();
        for (SgsApply record : records) {
            UacMember uacMember = uacMemberMapper.getById(record.getMemberId());
            if (null != uacMember) {
                record.setStaffName(uacMember.getStaffName());
                record.setTel(uacMember.getStasfTel());
                record.setEmail(uacMember.getEmail());
            }
        }
        volist.setRecords(records);
        volist.setTotalSize(page.getTotal());
        return volist;
    }

    public PageList<SgsApply> memberPassLisst(Integer pageSize, Integer pageNo, String value,
                                              int status, Timestamp startTime, Timestamp endTime) {
        PageList<SgsApply> volist = new PageList<>();
        LambdaQueryWrapper<SgsApply> sgsApplyLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sgsApplyLambdaQueryWrapper.eq(SgsApply::getSgsStatus, status);
        sgsApplyLambdaQueryWrapper.eq(SgsApply::getStatus, 0);
        sgsApplyLambdaQueryWrapper.like(SgsApply::getUscc, value).or().like(SgsApply::getMemberName, value);
        sgsApplyLambdaQueryWrapper.between(SgsApply::getUpdatedTime, startTime, endTime);
        sgsApplyLambdaQueryWrapper.orderByDesc(SgsApply::getUpdatedTime);
        Page<SgsApply> page = sgsApplyMapper.page(new Page<>(pageNo, pageSize), sgsApplyLambdaQueryWrapper);

        List<SgsApply> records = page.getRecords();
        for (SgsApply record : records) {
            UacMember uacMember = uacMemberMapper.getById(record.getMemberId());
            if (null != uacMember) {
                record.setStaffName(uacMember.getStaffName());
                record.setTel(uacMember.getStasfTel());
                record.setEmail(uacMember.getEmail());
            }
        }
        volist.setRecords(records);
        volist.setTotalSize(page.getTotal());
        return volist;
    }

    public void memberIdPass(Long memberId, Integer sgsStatus, String remark, Integer sgsType) throws Exception {
        // 银行认证通过,签发VC签名
        UacMember member = uacMemberMapper.getById(memberId);
        ChainApplyCredential chainApplyCredential = new ChainApplyCredential();
        chainApplyCredential.setCreateAt(LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        chainApplyCredential.setMemberId(member.getId());
        chainApplyCredential.setMemberName(member.getMemberName());
        chainApplyCredential.setMemberUscc(member.getUscCode());
        LambdaQueryWrapper<SgsConfiguration> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SgsConfiguration::getSgsType, sgsType);
        List<SgsConfiguration> sgsConfigurationList = sgsConfigurationMapper.selectList(queryWrapper);
        if (!sgsConfigurationList.isEmpty()) {
            SgsConfiguration sgsConfiguration = sgsConfigurationList.get(0);
            IssuerConfigVO issuerConfigVO = JSONObject.parseObject(sgsConfiguration.getIssuerConfig(), IssuerConfigVO.class);

            chainApplyCredential.setVcTemplateId(issuerConfigVO.getVcTemplateId());
            chainApplyCredential.setCredentialName(issuerConfigVO.getCredentialName());
            chainApplyCredential.setIssuerId(issuerConfigVO.getIssuerId());
            chainApplyCredential.setIssuerName(issuerConfigVO.getIssuerName());
            chainApplyCredential.setSgsId(sgsConfiguration.getId());
            chainApplyCredential.setSgsName(sgsConfiguration.getSgsName());
            chainApplyCredential.setSgsLogo(sgsConfiguration.getIco());
        }


        if (sgsStatus == SgsConstant.SgsStatus.TONGGUO.getValue()) {
            List<SgsApply> sgsApplies = sgsApplyMapper.lambdaQuery()
                    .eq(SgsApply::getMemberId, memberId)
                    .eq(SgsApply::getSgsStatus, 1)
                    .eq(SgsApply::getStatus, 0)
                    .eq(SgsApply::getSgsType, sgsType).list();
            SgsApply apply =sgsApplies.size()>0?sgsApplies.get(0):null;
            if (apply != null) {
                apply.setSgsStatus(sgsStatus);
                apply.setRevision(1);
                apply.setUpdatedTime(LocalDateTime.now());
                sgsApplyMapper.updateById(apply);
                if (apply.getSgsType() == 4) {
                    List<SgsCertInfo> list = this.lambdaQuery().eq(SgsCertInfo::getMemberId, memberId)
                            .eq(SgsCertInfo::getSgsStatus, 1).eq(SgsCertInfo::getSgsStatus, 0).list();
                    if (list.size() != 0) {
                        SgsCertInfo sgsCertInfo = list.get(0);
                        sgsCertInfo.setSgsStatus(2);
                        sgsCertInfo.setRevision(1);
                        this.updateById(sgsCertInfo);
                    }

                }
                if (apply.getSgsType() == 2) {

                    List<SgsBankInfo> list = sgsBankMapper.lambdaQuery().eq(SgsBankInfo::getMemberId, memberId)
                            .eq(SgsBankInfo::getSgsStatus, 1).eq(SgsBankInfo::getStatus, 0).list();
                    if (list.size() != 0) {
                        SgsBankInfo bankInfo = list.get(0);
                        bankInfo.setSgsStatus(2);
                        bankInfo.setRevision(1);
                        sgsBankMapper.updateById(bankInfo);

                        //VC资质
                        chainApplyCredential.setIsSign(true);
                        chainApplyCredential.setApplyStatus("认证成功");
                        if (apply.getStaffName()!=null) {
                            LambdaQueryWrapper<UacStaff> QueryWrapper = new LambdaQueryWrapper<>();
                            QueryWrapper.eq(UacStaff::getStaffName, apply.getStaffName());
                            List<UacStaff> uacStaffList = uacStaffMapper.selectList(QueryWrapper);
                            if (!uacStaffList.isEmpty()) {
                                chainApplyCredential.setStaffId(uacStaffList.get(0).getId().toString());
                            }

                        }
                    }
                }
            }
            UacMember uacMember = new UacMember();
            uacMember.setSgsStatus(1);
            uacMember.setRevision(1);
            uacMember.setId(memberId);
            uacMemberMapper.updateById(uacMember);
        } else {
            List<SgsApply> sgsApplies = sgsApplyMapper.lambdaQuery()
                    .eq(SgsApply::getMemberId, memberId).eq(SgsApply::getSgsStatus, 1).eq(SgsApply::getStatus, 0)
                    .eq(SgsApply::getSgsType, sgsType).list();
            SgsApply apply = !sgsApplies.isEmpty()?sgsApplies.get(0):null;
            if (apply != null) {
                apply.setSgsStatus(sgsStatus);
                apply.setRevision(1);
                apply.setRemark(remark);
                apply.setUpdatedTime(LocalDateTime.now());
                sgsApplyMapper.updateById(apply);
                if (apply.getSgsType() == 4) {
                    List<SgsCertInfo> list = this.lambdaQuery().eq(SgsCertInfo::getMemberId, memberId)
                            .eq(SgsCertInfo::getSgsStatus, 1).eq(SgsCertInfo::getStatus, 0).list();
                    if (!list.isEmpty()) {
                        list.get(0).setStatus(1);
                        this.updateById(list.get(0));
                    }
                }
                if (apply.getSgsType() == 2) {
                    List<SgsBankInfo> list = sgsBankMapper.lambdaQuery().eq(SgsBankInfo::getMemberId, memberId)
                            .eq(SgsBankInfo::getSgsStatus, 1).eq(SgsBankInfo::getStatus, 0).list();
                    if (!list.isEmpty()) {
                        list.get(0).setStatus(1);
                        sgsBankMapper.updateById(list.get(0));
                        // 申请失败
                        chainApplyCredential.setIsSign(false);
                        chainApplyCredential.setApplyStatus("认证失败");
                        if (apply.getStaffName()!=null) {
                            LambdaQueryWrapper<UacStaff> QueryWrapper = new LambdaQueryWrapper<>();
                            QueryWrapper.eq(UacStaff::getStaffName, apply.getStaffName());
                            List<UacStaff> uacStaffList = uacStaffMapper.selectList(QueryWrapper);
                            if (!uacStaffList.isEmpty()) {
                                chainApplyCredential.setStaffId(uacStaffList.get(0).getId().toString());
                             }

                        }
                    }
                }
            }
        }
        //申请数字凭证
        if (chainApplyCredential.getVcTemplateId()!=null) {
            mqSend.sendChain(JSONObject.toJSONString(chainApplyCredential), SgsConstant.VCSIGN_BANKAMOUNT);
        }
    }

    public PageList<UacMemberVO> memberListPage(Integer pageSize, Integer pageNo, String value, String remark, String startTime, String endTime) {
        PageList<UacMemberVO> pageList = new PageList<>();
        LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper = new LambdaQueryWrapper<>();

        uacMemberLambdaQueryWrapper.ne(UacMember::getKindId, 88L);
        uacMemberLambdaQueryWrapper.ne(UacMember::getKindId, 99L);
        uacMemberLambdaQueryWrapper.eq(UacMember::getStatus, 0);
        if (StringUtils.isNotEmpty(value)) {
            uacMemberLambdaQueryWrapper.like(UacMember::getUscCode, value).or()
                    .like(UacMember::getMemberName, value).or().like(UacMember::getStasfTel, value);
        }
        if (StringUtils.isNotEmpty(remark)) {
            uacMemberLambdaQueryWrapper.eq(UacMember::getRemark, remark);
        }
        if (StringUtils.isNotEmpty(startTime)) {
            uacMemberLambdaQueryWrapper.ge(UacMember::getCreatedTime, startTime);
        }
        if (StringUtils.isNotEmpty(endTime)) {
            uacMemberLambdaQueryWrapper.le(UacMember::getCreatedTime, endTime);
        }
        uacMemberLambdaQueryWrapper.orderByDesc(UacMember::getCreatedTime);
        Page<UacMember> uacMembers = uacMemberMapper.page(new Page<>(pageNo, pageSize), uacMemberLambdaQueryWrapper);
        if (!CommonUtil.isEmpty(uacMembers.getRecords())) {
            List<UacMemberVO> sgsDetils = findSgsDetils(uacMembers.getRecords());
            if (!CommonUtil.isEmpty(sgsDetils)) {
                pageList.setRecords(sgsDetils);
                pageList.setTotalSize(uacMembers.getTotal());
                return pageList;
            }
        }
        return null;
    }

    private List<UacMemberVO> findSgsDetils(List<UacMember> uacMembers) {

        List<UacMemberVO> list = new ArrayList<>();
        uacMembers.forEach(uac -> {
            UacMemberVO memberVO = new UacMemberVO();
            BeanUtils.copyProperties(uac, memberVO);
            List<SgsApply> infos = sgsApplyMapper.lambdaQuery()
                    .eq(SgsApply::getStatus, 0)
                    .eq(SgsApply::getMemberId, uac.getId())
                    .eq(SgsApply::getUserType, SgsConstant.SgsParent.MEMBER.getValue()).list();
            List<Object> objects = new ArrayList<>();
            List<String> type = new ArrayList<>();
            if (!CommonUtil.isEmpty(infos)) {
                infos.forEach(info -> {
                    type.add(info.getSgsTypeName());
                    switch (info.getSgsType()) {
                        case 2:
                            List<SgsBankInfo> sgsBank = sgsBankMapper.lambdaQuery()
                                    .eq(SgsBankInfo::getSgsApplyId, info.getId()).eq(SgsBankInfo::getStatus, 0).list();
                            if (!CommonUtil.isEmpty(sgsBank)) {
                                SgsBankInfoVO sgsBankInfoVO = new SgsBankInfoVO();
                                BeanUtils.copyProperties(sgsBank.get(0), sgsBankInfoVO);
                                sgsBankInfoVO.setSgsType(2);
                                sgsBankInfoVO.setSgsTypeName(info.getSgsTypeName());
                                objects.add(sgsBankInfoVO);
                            }
                            break;
                        case 3:
                            List<SgsSwInfo> sgsMemberOnly = sgsSwInfoMapper.lambdaQuery()
                                    .eq(SgsSwInfo::getSgsApplyId, info.getId()).eq(SgsSwInfo::getStatus, 0).list();
                            if (!CommonUtil.isEmpty(sgsMemberOnly)) {
                                SgsSwInfoVO sgsSwInfoVO = new SgsSwInfoVO();

                                BeanUtils.copyProperties(sgsMemberOnly.get(0), sgsSwInfoVO);
                                sgsSwInfoVO.setSgsType(3);
                                sgsSwInfoVO.setSgsTypeName(info.getSgsTypeName());
                                objects.add(sgsSwInfoVO);
                            }
                            break;
                        case 4:
                            List<SgsCertInfo> sgsMember = this.lambdaQuery()
                                    .eq(SgsCertInfo::getSgsApplyId, info.getId()).eq(SgsCertInfo::getStatus, 0).list();
                            if (!CommonUtil.isEmpty(sgsMember)) {
                                SgsCertInfoVO sgsSwInfoVO = new SgsCertInfoVO();

                                BeanUtils.copyProperties(sgsMember.get(0), sgsSwInfoVO);
                                sgsSwInfoVO.setSgsType(4);
                                sgsSwInfoVO.setSgsTypeName(info.getSgsTypeName());
                                objects.add(sgsSwInfoVO);
                            }
                            break;
                    }
                });
                if (!CommonUtil.isEmpty(objects)) {
                    memberVO.setSgsDetails(objects);
                }
                if (!CommonUtil.isEmpty(type)) {
                    memberVO.setSgsTypes(type);
                }
            }
            list.add(memberVO);
        });
        return list;
    }

    public void memberUpdateSgsStatus(Long memberId) {
        //修改企业认证状态
        UacMember uacMember = uacMemberMapper.getById(memberId);
        uacMember.setSgsStatus(0);
        uacMemberMapper.updateById(uacMember);
        //查询企业认证记录
        List<SgsApply> bymemberIdAll = sgsApplyMapper.lambdaQuery().eq(SgsApply::getMemberId, memberId).eq(SgsApply::getStatus, 0).list();
        //删除企业全部认证记录
        for (SgsApply sgsApply : bymemberIdAll) {
            sgsApply.setStatus(1);
            sgsApply.setUpdatedTime(LocalDateTime.now());
            sgsApplyMapper.updateById(sgsApply);
        }
        //查询企业银行对公账户认证记录
        List<SgsBankInfo> sgsBankInfolist = sgsBankMapper.lambdaQuery().eq(SgsBankInfo::getMemberId, memberId).eq(SgsBankInfo::getStatus, 0).list();
        //删除企业银行对公账户认证记录
        for (SgsBankInfo bankInfo : sgsBankInfolist) {
            bankInfo.setStatus(1);
            sgsBankMapper.updateById(bankInfo);
        }

        //查询企业银行对公账户认证记录
        List<SgsCertInfo> sgsCertInfos = this.lambdaQuery().eq(SgsCertInfo::getMemberId, memberId).eq(SgsCertInfo::getStatus, 0).list();
        //删除企业银行对公账户认证记录
        for (SgsCertInfo certInfo : sgsCertInfos) {
            certInfo.setStatus(1);
            this.updateById(certInfo);
        }

        //查询企业单一窗口认证记录
        List<SgsSwInfo> sgsSwInfos = sgsSwInfoMapper.lambdaQuery().eq(SgsSwInfo::getMemberId, memberId).eq(SgsSwInfo::getStatus, 0).list();
        //删除企业单一窗口认证记录
        for (SgsSwInfo swInfo : sgsSwInfos) {
            swInfo.setStatus(1);
            sgsSwInfoMapper.updateById(swInfo);
        }
    }

    public SgsUacMemberVO memberfindInfo(Long id, Integer sgsType) {
        SgsUacMemberVO sgsUacMemberVO = new SgsUacMemberVO();
        if (sgsType == 2) {
            List<SgsBankInfo> list = sgsBankMapper.lambdaQuery().eq(SgsBankInfo::getMemberId, id).eq(SgsBankInfo::getStatus, 0).list();
            if (list.size() != 0) {
                sgsUacMemberVO.setSgsBankInfo(list.get(0));
            }
        }
        if (sgsType == 4) {
            List<SgsCertInfo> list = this.lambdaQuery().eq(SgsCertInfo::getMemberId, id).eq(SgsCertInfo::getStatus, 0).list();
            if (list.size() != 0) {
                sgsUacMemberVO.setSgsCertInfo(list.get(0));
            }
        }
        return sgsUacMemberVO;

    }
}

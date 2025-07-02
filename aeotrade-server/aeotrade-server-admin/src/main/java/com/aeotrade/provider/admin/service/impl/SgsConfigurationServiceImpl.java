package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.adminVo.IssueConfigMd;
import com.aeotrade.provider.admin.config.IssueProperties;
import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.provider.admin.entiy.SgsApply;
import com.aeotrade.provider.admin.entiy.SgsBankInfo;
import com.aeotrade.provider.admin.entiy.SgsConfiguration;
import com.aeotrade.provider.admin.mapper.SgsConfigurationMapper;
import com.aeotrade.provider.admin.service.SgsConfigurationService;
import com.aeotrade.provider.admin.uacVo.IssuerConfigVO;
import com.aeotrade.provider.admin.uacVo.MemberSgsVO;
import com.aeotrade.provider.admin.uacVo.SgsConfigurationVO;
import com.aeotrade.utlis.CommonUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 后台认证列表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class SgsConfigurationServiceImpl extends ServiceImpl<SgsConfigurationMapper, SgsConfiguration> implements SgsConfigurationService {
    @Autowired
    private IssueProperties issueProperties;
    @Autowired
    private SgsApplyServiceImpl sgsInfoMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private SgsBankInfoServiceImpl sgsBankMapper;

    public List<SgsConfiguration> findListSort(Integer userType) {
        List<SgsConfiguration> list = this.lambdaQuery()
                .eq(SgsConfiguration::getStatus,0)
                .eq(SgsConfiguration::getUserType,userType)
                .orderByAsc(SgsConfiguration::getSort).list();
        return list;
    }

    public List<SgsConfigurationVO> findListStatus(Long memberId, Integer type) {
        List<SgsConfiguration> list = this.lambdaQuery().eq(SgsConfiguration::getStatus,0)
                .eq(SgsConfiguration::getSgsStatus,1)
                .eq(SgsConfiguration::getUserType,type)
                .orderByAsc(SgsConfiguration::getSort).list();

        List<SgsConfigurationVO> voList = new ArrayList<>();
        if (!CommonUtil.isEmpty(list)) {
            list.forEach(sgs -> {
                SgsConfigurationVO vo = new SgsConfigurationVO();
                if (sgs.getAuthToChain()!=null&&sgs.getAuthToChain()>0){
                    IssuerConfigVO issueConfigVo = JSONObject.parseObject(sgs.getIssuerConfig(), IssuerConfigVO.class);
                    vo.setIssuerId(issueConfigVo.getIssuerId());
                    vo.setIssuerName(issueConfigVo.getIssuerName());
                    vo.setVcTemplateId(issueConfigVo.getVcTemplateId());
                    vo.setCredentialName(issueConfigVo.getCredentialName());
                }
                if (issueProperties.getIssueConfig()!=null){
                    List<IssueConfigMd> issueConfig = issueProperties.getIssueConfig();
                    for (IssueConfigMd issueConfigVo : issueConfig) {
                        if (issueConfigVo.getSgsConfigurationId().equals(sgs.getId())){
                            vo.setIssuerId(issueConfigVo.getIssuerCertId().toString());
                            vo.setIssuerName(issueConfigVo.getIssuerName());
                            vo.setVcTemplateId(issueConfigVo.getVcTemplateId());
                            vo.setCredentialName(issueConfigVo.getCredentialName());
                        }
                    }
                }
                BeanUtils.copyProperties(sgs, vo);
                List<SgsApply> applies = sgsInfoMapper.lambdaQuery()
                        .eq(SgsApply::getStatus, 0)
                        .eq(SgsApply::getMemberId, memberId)
                        .eq(SgsApply::getSgsType, sgs.getSgsType())
                        .orderByDesc(SgsApply::getUpdatedTime).list();
                SgsApply infos = applies.size()>0?applies.get(0):null;
                if (CommonUtil.isEmpty(infos)) {
                    vo.setSgsUserStatus(SgsConstant.SgsStatus.NO.getValue());
                } else {
                    vo.setSgsUserStatus(infos.getSgsStatus());
                    vo.setSgsTime(infos.getCreatedTime());
                    if (infos.getSgsStatus() == 3) {
                        vo.setRemark(infos.getRemark());
                    }
                }
                voList.add(vo);
            });
            return voList;
        }
        return null;
    }

    public MemberSgsVO memberQuery(Long id) {
        MemberSgsVO vo = new MemberSgsVO();
        UacMember uacMember = uacMemberMapper.getById(id);
        if (vo != null) {
            vo.setUacMember(uacMember);
        }
        List<SgsBankInfo> list = sgsBankMapper.lambdaQuery()
                .eq(SgsBankInfo::getMemberId,id)
                .eq(SgsBankInfo::getStatus,0)
                .eq(SgsBankInfo::getUserType,SgsConstant.SgsParent.MEMBER.getValue()).list();
        if (!CommonUtil.isEmpty(list)) {
            vo.setSgsBank(list.get(0));
        }
        return vo;
    }
}

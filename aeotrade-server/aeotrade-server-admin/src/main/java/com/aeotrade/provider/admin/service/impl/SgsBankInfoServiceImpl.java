package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.base.model.ChainApplyCredential;
import com.aeotrade.provider.admin.entiy.SgsConfiguration;
import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.provider.admin.entiy.SgsApply;
import com.aeotrade.provider.admin.entiy.SgsBankInfo;
import com.aeotrade.provider.admin.event.MemberBankAuthenticationEvent;
import com.aeotrade.provider.admin.mapper.SgsBankInfoMapper;
import com.aeotrade.provider.admin.mapper.SgsConfigurationMapper;
import com.aeotrade.provider.admin.service.SgsBankInfoService;
import com.aeotrade.provider.admin.uacVo.SgsBankDto;
import com.aeotrade.service.MqSend;
import com.aeotrade.utlis.CommonUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * <p>
 * 银行认证 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class SgsBankInfoServiceImpl extends ServiceImpl<SgsBankInfoMapper, SgsBankInfo> implements SgsBankInfoService {
    @Autowired
    private SgsApplyServiceImpl sgsInfoMapper;
    @Value("${aeotrade.ops-mail-recipients:}")
    private String recipients;
    @Value("${hmtx.messageDetails:}")
    private String messageDetails;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private MqSend mqSend;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private SgsConfigurationMapper sgsConfigurationMapper;

    @Transactional
    public SgsBankDto sgsBankSave(SgsBankDto sgsBankDto) {
        SgsApply sgsInfo = new SgsApply();
        sgsInfo.setMemberId(sgsBankDto.getMemberId());
        sgsInfo.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
        sgsInfo.setSgsType(SgsConstant.SgsType.MEMBER_BANK.getValue());
        sgsInfo.setSgsTypeName(sgsBankDto.getSgsTypeName());
        sgsInfo.setSgsStatus(SgsConstant.SgsStatus.WEIRENZ.getValue());
        sgsInfo.setMemberName(sgsBankDto.getMemberName());
        sgsInfo.setUscc(sgsBankDto.getUscc());
        sgsInfo.setStatus(0);
        sgsInfo.setCreatedTime(LocalDateTime.now());
        sgsInfo.setUpdatedTime(LocalDateTime.now());
        sgsInfoMapper.save(sgsInfo);
        Random random=new Random();
        int nextInt = random.nextInt(10);
        sgsBankDto.setDeduction("0.0".concat(String.valueOf(nextInt==0?1:nextInt)));
        sgsBankDto.setSgsApplyId(sgsInfo.getId());
        sgsBankDto.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
        sgsBankDto.setSgsStatus(SgsConstant.SgsStatus.WEIRENZ.getValue());
        sgsBankDto.setStatus(0);
        this.save(sgsBankDto);
        if (!StringUtils.isEmpty(recipients)) {
            String recipient = recipients;
            String[] serndEmial = recipient.split(",");
            /**2.发送认证事件*/
            MemberBankAuthenticationEvent memberAuthenticationEvent = new MemberBankAuthenticationEvent(this,sgsBankDto,serndEmial);
            eventPublisher.publishEvent(memberAuthenticationEvent);
        }
        return sgsBankDto;
    }


    public Map<String, Object> findBankMoney( String bankMoney, Long memberId
                                            ,String vcTemplateId,
                                               String credentialName,
                                               String issuerId,
                                               String issuerName,
                                               String staffId,
                                              String sgsConfigId
    ) {
        SgsBankInfo sgsBank = new SgsBankInfo();
        sgsBank.setMemberId(memberId);
        sgsBank.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
        List<SgsBankInfo> list =  this.lambdaQuery()
                .eq(SgsBankInfo::getMemberId,memberId)
                .eq(SgsBankInfo::getStatus,0)
                .eq(SgsBankInfo::getUserType,SgsConstant.SgsParent.MEMBER.getValue())
                .orderByDesc(SgsBankInfo::getCreatedTime).list();
        if(!CommonUtil.isEmpty(list)){
            SgsBankInfo bank = list.get(0);
            String msgType;
            ChainApplyCredential chainApplyCredential = new ChainApplyCredential();
            Map<String ,Object> map = new HashMap<>();
            List<SgsApply> infos = sgsInfoMapper.lambdaQuery().eq(SgsApply::getMemberId,memberId)
                    .eq(SgsApply::getUserType,SgsConstant.SgsParent.MEMBER.getValue())
                    .eq(SgsApply::getStatus,0)
                    .eq(SgsApply::getSgsType,SgsConstant.SgsType.MEMBER_BANK.getValue()).list();
            if (bank.getDeduction().equals(bankMoney)) {
                //认证成功
                map.put("bank",1 );
                map.put("uscCode",bank.getUscc());
                sgsBank.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
                sgsBank.setRevision(1);
                sgsBank.setId(bank.getId());
                this.updateById(sgsBank);
                if(!CommonUtil.isEmpty(infos)){
                    SgsApply info = infos.get(0);
                    info.setRevision(1);
                    info.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
                    info.setUpdatedTime(LocalDateTime.now());
                    sgsInfoMapper.updateById(info);
                }
                UacMember uacMember = new UacMember();
                uacMember.setRevision(1);
                uacMember.setMemberName(bank.getMemberName());
                uacMember.setUscCode(bank.getUscc());
                uacMember.setSgsStatus(1);
                uacMember.setId(memberId);
                uacMemberMapper.updateById(uacMember);
                HashMap<String, String> messageMap = new HashMap<>();
                messageMap.put("receive_type", "2");
                messageMap.put("receive_id", String.valueOf(uacMember.getId()));
                messageMap.put("receive_name", uacMember.getMemberName());
                messageMap.put("details_type", "2");
                messageMap.put("details_button","立即前往");
                messageMap.put("message_details",messageDetails);
                messageMap.put("message_source","基础平台");
                messageMap.put("template_number","GG2024000001");
                mqSend.sendMessage(JSONObject.toJSONString(messageMap),"GG2024000001");
                // 银行认证通过,签发VC签名
                chainApplyCredential.setMemberId(memberId);
                chainApplyCredential.setMemberName(bank.getMemberName());
                chainApplyCredential.setMemberUscc(bank.getUscc());
                chainApplyCredential.setIsSign(true);
                chainApplyCredential.setApplyStatus("认证成功");
                chainApplyCredential.setVcTemplateId(vcTemplateId);
                chainApplyCredential.setCredentialName(credentialName);
                chainApplyCredential.setIssuerId(issuerId);
                chainApplyCredential.setIssuerName(issuerName);
                chainApplyCredential.setStaffId(staffId);
                msgType=SgsConstant.VCSIGN_BANKAMOUNT;
            } else {
                //认证失败
                map.put("bank",0 );
                map.put("uscCode",bank.getUscc());
                sgsBank.setSgsStatus(SgsConstant.SgsStatus.SHIBAI.getValue());
                sgsBank.setRevision(1);
                sgsBank.setId(bank.getId());
                this.updateById(sgsBank);
                if(!CommonUtil.isEmpty(infos)){
                    SgsApply info = infos.get(0);
                    info.setRevision(1);
                    info.setSgsStatus(SgsConstant.SgsStatus.SHIBAI.getValue());
                    info.setUpdatedTime(LocalDateTime.now());
                    sgsInfoMapper.updateById(info);
                }
                list.forEach(i->{
                    i.setStatus(1);
                    this.updateById(i);
                });

                infos.forEach(in->{
                    in.setStatus(1);
                    in.setUpdatedTime(LocalDateTime.now());
                    sgsInfoMapper.updateById(in);
                });
                // 银行认证失败，记录认证结果
                chainApplyCredential.setMemberId(bank.getMemberId());
                chainApplyCredential.setMemberName(bank.getMemberName());
                chainApplyCredential.setMemberUscc(bank.getUscc());
                chainApplyCredential.setIsSign(false);
                chainApplyCredential.setApplyStatus("认证失败");
                chainApplyCredential.setVcTemplateId(vcTemplateId);
                chainApplyCredential.setCredentialName(credentialName);
                chainApplyCredential.setIssuerId(issuerId);
                chainApplyCredential.setIssuerName(issuerName);
                chainApplyCredential.setStaffId(staffId);
                msgType=SgsConstant.VCSIGN_BANKAMOUNT;
            }
            if (!StringUtils.isEmpty(sgsConfigId)) {
                chainApplyCredential.setSgsId(Long.valueOf(sgsConfigId));
                SgsConfiguration sgsConfiguration = sgsConfigurationMapper.selectById(sgsConfigId);
                if (sgsConfiguration != null) {
                    chainApplyCredential.setSgsName(sgsConfiguration.getSgsName());
                    chainApplyCredential.setSgsLogo(sgsConfiguration.getIco());
                }
            }
            chainApplyCredential.setCreateAt(LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            mqSend.sendChain(JSONObject.toJSONString(chainApplyCredential),msgType);
            return map;
        }
        return null;
    }

    public void supplementaryBankMoneyAuthentication(String vcTemplateId,
                                                     String credentialName,
                                                     String issuerId,
                                                     String issuerName,
                                                     String sgsConfigId,
                                                     Integer sgsType,
                                                     String msgType
    ) {
        List<SgsApply> infos = sgsInfoMapper.lambdaQuery()
                .eq(SgsApply::getUserType, SgsConstant.SgsParent.MEMBER.getValue())
                .eq(SgsApply::getSgsStatus, SgsConstant.SgsStatus.TONGGUO.getValue())
                .eq(SgsApply::getSgsType, sgsType).list();
        for (SgsApply info : infos) {
            ChainApplyCredential chainApplyCredential = new ChainApplyCredential();

            // 银行认证通过,签发VC签名
            chainApplyCredential.setMemberId(info.getMemberId());
            chainApplyCredential.setMemberName(info.getMemberName());
            chainApplyCredential.setMemberUscc(info.getUscc());
            chainApplyCredential.setIsSign(true);
            chainApplyCredential.setApplyStatus("认证成功");
            chainApplyCredential.setVcTemplateId(vcTemplateId);
            chainApplyCredential.setCredentialName(credentialName);
            chainApplyCredential.setIssuerId(issuerId);
            chainApplyCredential.setIssuerName(issuerName);
            UacMember uacMember = uacMemberMapper.get(info.getMemberId());
            if (uacMember==null){
                continue;
            }
            chainApplyCredential.setStaffId(uacMember.getStaffId().toString());

            if (!StringUtils.isEmpty(sgsConfigId)) {
                chainApplyCredential.setSgsId(Long.valueOf(sgsConfigId));
                SgsConfiguration sgsConfiguration = sgsConfigurationMapper.selectById(sgsConfigId);
                if (sgsConfiguration != null) {
                    chainApplyCredential.setSgsName(sgsConfiguration.getSgsName());
                    chainApplyCredential.setSgsLogo(sgsConfiguration.getIco());
                }
            }
            chainApplyCredential.setCreateAt(info.getCreatedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            mqSend.sendChain(JSONObject.toJSONString(chainApplyCredential), msgType);

        }

    }
}

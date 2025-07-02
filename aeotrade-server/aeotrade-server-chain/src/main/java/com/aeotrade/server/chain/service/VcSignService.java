package com.aeotrade.server.chain.service;

import com.aeotrade.base.model.ChainApplyCredential;
import com.aeotrade.chainmaker.event.ApplyCredentialEvent;
import com.aeotrade.chainmaker.model.ChainApplyCredentialsLogs;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.repository.ChainApplyCredentialsLogsMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.server.chain.ChainTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VcSignService {
    @Value("${hmtx.browser.chain-id:aeotradechain}")
    private String HMTX_BROWSER_CHAINID;
    private final ChainCertUserMemberMapper chainCertUserMemberMapper;
    private final ChainTransactionService chainTransactionService;
    private final ChainApplyCredentialsLogsMapper chainApplyCredentialsLogsMapper;
    private final ApplicationEventPublisher eventPublisher;

    public VcSignService(ChainCertUserMemberMapper chainCertUserMemberMapper, ChainTransactionService chainTransactionService, ChainApplyCredentialsLogsMapper chainApplyCredentialsLogsMapper, ApplicationEventPublisher eventPublisher) {
        this.chainCertUserMemberMapper = chainCertUserMemberMapper;
        this.chainTransactionService = chainTransactionService;
        this.chainApplyCredentialsLogsMapper = chainApplyCredentialsLogsMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 签发者颁发可验证凭证VC
     */
    public void issueVc(String memberId,String memberName,String memberUscc,String vcTemplateId,String credentialName,
                        String issuer,Long sgsConfigId,String sgsName,String sgsLogo) {
        // 签发VC
        ChainCertUserMember chainCertUserMember = new ChainCertUserMember();
        chainCertUserMember.setDid(issuer);
        chainCertUserMember.setIsDel(false);
        List<ChainCertUserMember> chainCertUserMemberList = chainCertUserMemberMapper.findAll(Example.of(chainCertUserMember));
        if(chainCertUserMemberList.isEmpty()){
            log.warn("{} 签名，未查到该机构DID标识",issuer);
            return;
        }
        ChainClient chainClient = chainTransactionService.client(issuer, HMTX_BROWSER_CHAINID, chainCertUserMemberList.get(0));

        ChainApplyCredential chainApplyCredential = new ChainApplyCredential();

        //过期时间
        chainApplyCredential.setExpirationDate(String.valueOf(LocalDate.now().getYear()).charAt(0)+"999-12-31");
        chainApplyCredential.setMemberId(Long.parseLong(memberId));
        chainApplyCredential.setMemberName(memberName);
        chainApplyCredential.setMemberUscc(memberUscc);
        chainApplyCredential.setVcTemplateId(vcTemplateId);
        chainApplyCredential.setCredentialName(credentialName);
        chainApplyCredential.setIssuerId(issuer);
        chainApplyCredential.setIssuerName(chainCertUserMemberList.get(0).getTenantName());
        chainApplyCredential.setSgsId(sgsConfigId);
        chainApplyCredential.setSgsName(sgsName);
        chainApplyCredential.setSgsLogo(sgsLogo);

        //subject
        Map<String,Object> subject = new HashMap<>();
        subject.put("memberName",memberName);
        subject.put("memberUscc",memberUscc);

        chainApplyCredential.setSubject(subject);

        ApplyCredentialEvent applyCredentialEvent = new ApplyCredentialEvent(this,chainApplyCredential,chainClient);
        eventPublisher.publishEvent(applyCredentialEvent);
    }

    /**
     * 记录签发状态
     */
    public void issueRecord(ChainApplyCredentialsLogs applyCredentialsLogs) {
        chainApplyCredentialsLogsMapper.save(applyCredentialsLogs);
    }

    public Page<ChainApplyCredentialsLogs> findCredentialsLogs(Long memberId, Pageable pageable) {
        ChainApplyCredentialsLogs chainApplyCredentialsLogs = new ChainApplyCredentialsLogs();
        chainApplyCredentialsLogs.setMemberId(memberId);
        Example<ChainApplyCredentialsLogs> example = Example.of(chainApplyCredentialsLogs);
        return chainApplyCredentialsLogsMapper.findAll(example,pageable);
    }
}

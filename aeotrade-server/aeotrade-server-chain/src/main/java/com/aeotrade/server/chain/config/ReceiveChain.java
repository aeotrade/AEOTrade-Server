package com.aeotrade.server.chain.config;

import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.base.model.ChainApplyCredential;
import com.aeotrade.chainmaker.constant.UserTypeEnum;
import com.aeotrade.chainmaker.model.ChainApplyCredentialsLogs;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.server.chain.AeotradeCaService;
import com.aeotrade.server.chain.service.VcSignService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.data.domain.Example;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@EnableBinding(MqChainReceiveConfig.class)
public class ReceiveChain {
    @Autowired
    private IssuingAgenciesProperties issuingAgenciesProperties;
    @Autowired
    private AeotradeCaService aeotradeCaService;
    @Autowired
    private ChainCertUserMemberMapper chainCertUserMemberMapper;
    @Autowired
    private VcSignService vcSignService;

    /**
     * 消息处理
     */
    @StreamListener(MqChainReceiveConfig.CHAINSERVICE)
    public void receiveMessage(@Header(value = "type",defaultValue = "userSign", required = false) String type, Message<String> message) throws Exception {
        log.info(message.getPayload());
        switch (type) {
            case SgsConstant.VCSIGN_BANKAMOUNT:
                handleTypeBankAuth(message);
                break;
            case SgsConstant.VCSIGN_BJSINGLEWINDOW:
                handleTypeBjsinglewindowAuth(message);
                break;
            default:
                userSign(message);
        }
    }

    /**
     * 用户证书链上签名
     */
    private void userSign(Message<String> message) throws Exception {
        MessageVo vo = JSON.parseObject(message.getPayload(), MessageVo.class);
        //查询是否为主管理员
        ChainCertUserMember chainCertUserMember = new ChainCertUserMember();
        chainCertUserMember.setTenantId(vo.getTenantId());
        chainCertUserMember.setUserId(vo.getUserId()==null?"010":vo.getUserId());// 010 默认管理员标识
        chainCertUserMember.setCertType(UserTypeEnum.ADMIN.getIndex());
        chainCertUserMember.setIsDel(false);
        Example<ChainCertUserMember> exampleOne = Example.of(chainCertUserMember);
        List<ChainCertUserMember> tenantList = chainCertUserMemberMapper.findAll(exampleOne);
        if(tenantList.isEmpty()){
            log.info("开始创建企业默认管理员了++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            // 010 默认管理员标识
            aeotradeCaService.genCaCert(vo.getTenantId(), vo.getTenantName(), vo.getUscc(), vo.getCreatTime(), vo.getUserType(), chainCertUserMember.getUserId(), vo.getRoleCodeRulesEnum(), vo.getChainId(), UserTypeEnum.ADMIN);
        }else {
            log.info("开始创建企业员工了--------------------------------------------------------------------------------------------");
            aeotradeCaService.genCaCert(vo.getTenantId(), vo.getTenantName(), vo.getUscc(), vo.getCreatTime(), vo.getUserType(), chainCertUserMember.getUserId(), vo.getRoleCodeRulesEnum(), vo.getChainId(), UserTypeEnum.CLIENT);
        }
    }

    /**
     * 银行卡金额认证后处理
     */
    private void handleTypeBankAuth(Message<String> message) {
        ChainApplyCredential chainApplyCredential = JSONObject.parseObject(message.getPayload(), ChainApplyCredential.class);
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        if (chainApplyCredential.getIsSign()){
            vcSignService.issueVc(chainApplyCredential.getMemberId().toString(),chainApplyCredential.getMemberName(),
                    chainApplyCredential.getMemberUscc(),chainApplyCredential.getVcTemplateId(),
                    chainApplyCredential.getCredentialName(),chainApplyCredential.getIssuerId(),
            chainApplyCredential.getSgsId(),chainApplyCredential.getSgsName(),chainApplyCredential.getSgsLogo());
        }
        vcSignService.issueRecord(new ChainApplyCredentialsLogs(null,
                chainApplyCredential.getMemberId(), null, null,
                chainApplyCredential.getVcTemplateId(),
                chainApplyCredential.getCredentialName(),
                chainApplyCredential.getIssuerId(),
                chainApplyCredential.getIssuerName(),
                chainApplyCredential.getApplyStatus(),
                LocalDateTime.parse(chainApplyCredential.getCreateAt(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                localDateTime));

    }

    /**
     * 北京单一窗口认证后处理
     */
    private void handleTypeBjsinglewindowAuth(Message<String> message) {
        ChainApplyCredential chainApplyCredential = JSONObject.parseObject(message.getPayload(), ChainApplyCredential.class);
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        if (chainApplyCredential.getIsSign()) {
            vcSignService.issueVc(chainApplyCredential.getMemberId().toString(), chainApplyCredential.getMemberName(),
                    chainApplyCredential.getMemberUscc(), chainApplyCredential.getVcTemplateId(),
                    chainApplyCredential.getCredentialName(), chainApplyCredential.getIssuerId(),
                    chainApplyCredential.getSgsId(), chainApplyCredential.getSgsName(), chainApplyCredential.getSgsLogo());
        }
        vcSignService.issueRecord(new ChainApplyCredentialsLogs(null,
                chainApplyCredential.getMemberId(), null, null,
                chainApplyCredential.getVcTemplateId(),
                chainApplyCredential.getCredentialName(),
                chainApplyCredential.getIssuerId(),
                chainApplyCredential.getIssuerName(),
                chainApplyCredential.getApplyStatus(),
                LocalDateTime.parse(chainApplyCredential.getCreateAt(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                localDateTime));
    }

    /**
     * 通知类消息失败处理
     */
    @ServiceActivator(inputChannel = "queue.chain.chaingroup.errors")
    public void error(Message<String> message) {
        log.warn("message:{}" , message );
    }


}

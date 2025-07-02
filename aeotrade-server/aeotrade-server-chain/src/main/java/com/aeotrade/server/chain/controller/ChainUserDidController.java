package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.model.*;
import com.aeotrade.chainmaker.repository.ChainApplyCredentialsLogsMapper;
import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.did.contract.DidContractService;
import com.aeotrade.server.did.repository.MongoClazzRepository;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class ChainUserDidController extends BaseController {
    private final ChainApplyCredentialsLogsMapper chainApplyCredentialsLogsMapper;
    @Value("${hmtx.chain.did.contract-admin-did:}")
    private String contractAdminDid;
    private final MongoClazzRepository mongoClazzRepository;
    private final DidContractService didContractService;
    private final ChainTransactionService chainTransactionService;

    public ChainUserDidController(MongoClazzRepository mongoClazzRepository, DidContractService didContractService, ChainTransactionService chainTransactionService, ChainApplyCredentialsLogsMapper chainApplyCredentialsLogsMapper) {
        this.mongoClazzRepository = mongoClazzRepository;
        this.didContractService = didContractService;
        this.chainTransactionService = chainTransactionService;
        this.chainApplyCredentialsLogsMapper = chainApplyCredentialsLogsMapper;
    }

    /**
     * 查询当前企业下的VC列表
     * @param memberId 企业标识
     */
    @GetMapping("/did/vc/list")
    public Object getVcListByDid(@RequestParam(required = true) String memberId) throws Exception {
        List<JSONObject> list = new ArrayList<>();
        Query query = new Query(Criteria.where("memberId").is(Long.parseLong(memberId)));
        List<ChainUserDids> chainUserDids = mongoClazzRepository.getMongoTemplate().find(query, ChainUserDids.class);
        for (ChainUserDids chainUserDid : chainUserDids){
            for (ChainDid chainDid : chainUserDid.getDids()){
                List<ChainCredential> chainCredentials = chainDid.getCredentials();
                for (ChainCredential chainCredential : chainCredentials){
                    JSONObject vcJsonObject = chainCredential.getVc();
                    List<String> vcRevokedList = didContractService.getVCRevokedListFromChain(chainTransactionService.getChainClientByDid(contractAdminDid), chainCredential.getVcId(), 0, 0);
                    //当前状态
                    if (vcRevokedList != null && vcRevokedList.contains(chainCredential.getVcId())) {
                        vcJsonObject.put("status", "revoke");
                    } else {
                        vcJsonObject.put("status", "normal");
                        Date expirationDate = vcJsonObject.getDate("expirationDate");
                        if (expirationDate != null && expirationDate.getTime() <= System.currentTimeMillis()) {
                            vcJsonObject.put("status", "loseEfficacy");
                        }
                    }
                    //颁发机构
                    if (vcJsonObject.getString("issuer") != null) {
                        String issuerDid = vcJsonObject.getString("issuer");
                        String vcTemplateId = vcJsonObject.getJSONObject("template").getString("id");
                        ChainApplyCredentialsLogs chainApplyCredentialsLogs=new ChainApplyCredentialsLogs();
                        chainApplyCredentialsLogs.setMemberId(Long.parseLong(memberId));
                        chainApplyCredentialsLogs.setIsuserId(issuerDid);
                        chainApplyCredentialsLogs.setVcTemplateId(vcTemplateId);
                        List<ChainApplyCredentialsLogs> chainApplyCredentialsLogsList = chainApplyCredentialsLogsMapper.findAll(Example.of(chainApplyCredentialsLogs));
                        if (!chainApplyCredentialsLogsList.isEmpty()) {
                            vcJsonObject.put("issuerName", chainApplyCredentialsLogsList.get(0).getIsuserName());
                        }else {
                            vcJsonObject.put("issuerName", "...");
                        }
                    }
                    //认证类型名称
                    vcJsonObject.put("voucherTypeName","...");
                    //认证类型logo
                    vcJsonObject.put("voucherLogo","");

                    ChainCredentialSgsConfig sgsConfig = chainCredential.getSgsConfig();
                    if (sgsConfig!=null){
                        vcJsonObject.put("voucherLogo",sgsConfig.getSgsLogo());
                        vcJsonObject.put("voucherTypeName",sgsConfig.getSgsName());
                    }

                    list.add(vcJsonObject);
                }

            }
        }
        return handleResult(list);
    }

    /**
     * vc日志
     * @param memberId 企业标识
     */
    @GetMapping("/did/vc/apply/log")
    public Object getVcLogByDid(@RequestParam(required = true) String memberId,
                                @RequestParam(defaultValue = "0") Integer pageNo,
                                @RequestParam(defaultValue = "10") Integer pageSize) throws Exception {
        try {
            pageNo -= 1;
            // 校验分页参数
            if (pageNo < 0) {
                pageNo = 0;
            }
            if (pageSize < 0) {
                pageSize = 10;
            }

            ChainApplyCredentialsLogs chainApplyCredentialsLogs = new ChainApplyCredentialsLogs();
            chainApplyCredentialsLogs.setMemberId(Long.parseLong(memberId));

            Page<ChainApplyCredentialsLogs> chainApplyCredentialsLogsPage = chainApplyCredentialsLogsMapper.findAll(
                    Example.of(chainApplyCredentialsLogs), PageRequest.of(pageNo, pageSize));

            PageList<ChainApplyCredentialsLogs> pageList = new PageList<>();
            pageList.setSize(pageSize);
            pageList.setCurrent(pageNo);
            pageList.setTotalSize(chainApplyCredentialsLogsPage.getTotalElements());
            pageList.setRecords(chainApplyCredentialsLogsPage.toList());

            return handleResultList(pageList);
        } catch (NumberFormatException e) {
            // 处理 memberId 转换异常
            return handleFail("Invalid memberId format");
        }
    }

}

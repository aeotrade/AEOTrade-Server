package com.aeotrade.server.chain.config;

import com.aeotrade.chainmaker.event.CertSignSuccessEvent;
import com.aeotrade.chainmaker.exception.AeoChainException;
import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.utlis.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class Chongxingshanglian {
    @Value("${hmtx.browser.chain-id}")
    private String HMTX_BROWSER_CHAINID;
    @Value("${hmtx.browser.server-url}")
    private String HMTX_BROWSER_SERVER;
    @Value("${hmtx.chain.cert-auto-retry:false}")
    private Boolean HMTX_CHAIN_CERT_AUTO_RETRY;
    @Value("${hmtx.chain.did.contract-admin-did:}")
    private String contractAdminDid;
    @Autowired
    private ChainCertCaTenantMapper chainCertCaTenantMapper;
    @Autowired
    private ChainTransactionService chainTransactionService;
    @Autowired
    private ChainSdkCertMapper chainSdkCertMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Scheduled(fixedDelay = 300000)
    public void task1() {
        if (!HMTX_CHAIN_CERT_AUTO_RETRY){
            log.info("未开启CA证书自动上链");
            return;
        }
        try {
            ChainCertCaTenant chainCertCaTenant = chainCertCaTenantMapper.findTop1ByisChainAuthAndIsWhetherAndIsDel(false,true, false);
            if (chainCertCaTenant == null)
                return;
            log.info("未上链数据: {}" , chainCertCaTenant );
            shanglian("0001001", chainCertCaTenant, chainCertCaTenant.getChainId());
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new AeotradeException(e.getMessage());
        }
    }

    public void shanglian(String userId, ChainCertCaTenant chainCertCaTenant, String chainId) {
        try {
            //链上身份鉴权
            chainTransactionService.addCa(userId, chainCertCaTenant, chainId);
            //记录数据库
            chainCertCaTenant.setIsChainAuth(true);
            chainCertCaTenant.setIsVote(true);
            chainCertCaTenant.setUpdateAt(new Date());
            chainCertCaTenant.setIsWhether(false);
            log.info("++++++++++++++++++++++++++++++++++++++" + chainCertCaTenant.getIsChainAuth() + "++++++++++++++++++++++++++++++++++++++++++++++++");
            chainCertCaTenantMapper.save(chainCertCaTenant);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new AeoChainException(e.getMessage());
        }
    }
    @Scheduled(cron = "0 */5 * * * ?")
    public void task2() {
        findChainDecimal();

    }
    @Scheduled(cron = "0 */4 * * * ?")
    public void task3() {
        findChainGetBlockList();
    }
    @Scheduled(cron = "0 */6 * * * ?")
    public void task4() {
        findChainGetTxList();
    }


    public void findChainDecimal(){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("ChainId",HMTX_BROWSER_CHAINID);
            map.put("cmb","Decimal");
            String Decimal = HttpRequestUtils.httpGet(HMTX_BROWSER_SERVER, map);
            redisTemplate.opsForValue().set("AEOTRADECHAIN:Decimal",Decimal);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
    public void findChainGetBlockList(){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("ChainId",HMTX_BROWSER_CHAINID);
            map.put("Limit","20");
            map.put("cmb","GetBlockList");
            String GetBlockList = HttpRequestUtils.httpGet(HMTX_BROWSER_SERVER, map);
            redisTemplate.opsForValue().set("AEOTRADECHAIN:GetBlockList",GetBlockList);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }
    public void findChainGetTxList(){
        try {
            Map<String,Object> map=new HashMap<>();
            map.put("ChainId",HMTX_BROWSER_CHAINID);
            map.put("Limit","20");
            map.put("cmb","GetTxList");
            String GetTxList = HttpRequestUtils.httpGet(HMTX_BROWSER_SERVER, map);
            redisTemplate.opsForValue().set("AEOTRADECHAIN:GetTxList",GetTxList);
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    // 历史数据处理: 为组织下已经存在的企业员工自动生成 DID
    @Scheduled(fixedDelay = 5000)
    public void taskForDID() {
        Query query = new Query();
        query.addCriteria(Criteria.where("did").in(null, ""));
        query.limit(10);
        List<ChainCertUserMember> chainCertUserMembers = mongoTemplate.find(query, ChainCertUserMember.class);
        List<ChainCertCaTenant> chainCertCaTenants = mongoTemplate.find(query, ChainCertCaTenant.class);

        if (chainCertUserMembers.isEmpty() && chainCertCaTenants.isEmpty()) {
            return;
        }

        try {
            ChainClient chainClient;
            if (StringUtils.isEmpty(contractAdminDid)) {
                chainClient = chainTransactionService.client("10010", HMTX_BROWSER_CHAINID);
            }else {
                chainClient = chainTransactionService.getChainClientByDid(contractAdminDid);
            }

            processMembers(chainCertUserMembers, chainClient, ChainCertUserMember::getSignCertId);
            processMembers(chainCertCaTenants, chainClient, ChainCertCaTenant::getCertId);
        } catch (Exception e) {
            log.warn("为历史数据生成DID标识时发生异常", e);
        }
    }

    private <T> void processMembers(List<T> members, ChainClient chainClient, Function<T, Long> certIdExtractor) {
        if (!members.isEmpty()) {
            members.forEach(member -> {
                Optional<ChainSdkCert> chainSdkCertOptional = chainSdkCertMapper.findById(certIdExtractor.apply(member));
                if (chainSdkCertOptional.isEmpty()) {
                    log.warn("未查到证书信息, {} ", certIdExtractor.apply(member));
                } else {
                    // 发布生成DID事件
                    CertSignSuccessEvent certSignSuccessEvent;
                    if (member instanceof ChainCertUserMember) {
                        ChainCertUserMember userMember = (ChainCertUserMember) member;
                        certSignSuccessEvent = new CertSignSuccessEvent(userMember,
                                userMember.getTenantId(),
                                userMember.getUserId(),
                                "_id", userMember.getId(),
                                chainSdkCertOptional.get().getCert(),
                                chainSdkCertOptional.get().getPrivateKey(), chainClient);
                    } else if (member instanceof ChainCertCaTenant) {
                        ChainCertCaTenant caTenant = (ChainCertCaTenant) member;
                        certSignSuccessEvent = new CertSignSuccessEvent(caTenant,
                                caTenant.getTenantId(),
                                null,
                                "_id", caTenant.getId(),
                                chainSdkCertOptional.get().getCert(),
                                chainSdkCertOptional.get().getPrivateKey(), chainClient);
                    } else {
                        log.warn("未知的成员类型: {}", member.getClass().getName());
                        return;
                    }
                    eventPublisher.publishEvent(certSignSuccessEvent);
                }
            });
        }
    }

}
    

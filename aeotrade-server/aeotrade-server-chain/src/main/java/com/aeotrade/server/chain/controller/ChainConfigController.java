package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.chain.service.IChainCertCaTenantService;
import com.aeotrade.suppot.BaseController;
import org.chainmaker.pb.config.ChainConfigOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 调整链配置
 */
@RestController
public class ChainConfigController extends BaseController {
    private static final Logger log = LoggerFactory.getLogger(ChainConfigController.class);
    private final ChainTransactionService chainTransactionService;
    private final IChainCertCaTenantService chainCertCaTenantService;

    public ChainConfigController(ChainTransactionService chainTransactionService,
                                 IChainCertCaTenantService chainCertCaTenantService) {
        this.chainTransactionService = chainTransactionService;
        this.chainCertCaTenantService = chainCertCaTenantService;
    }

    /**
     * 查询没有签名的CA组织,并将数据表中记录组织的状态修改为正确状态
     * @param chainId
     * @throws ChainClientException
     * @throws ChainMakerCryptoSuiteException
     */
    @PostMapping("/config/ca")
    public Object queryTrustRootsAndModifyStatus(@RequestParam(defaultValue = "chain1", required = false) String chainId) throws ChainClientException, ChainMakerCryptoSuiteException {
        //查询链签名CA中的所有组织名称
        ChainClient chain1 = chainTransactionService.client("10010", chainId);
        ChainConfigOuterClass.ChainConfig chainConfig = chain1.getChainConfig(10000L);
        List<String> caList = chainConfig.getTrustRootsList().stream().map(trustRoot -> {
            return trustRoot.getOrgId().trim();
        }).collect(Collectors.toList());
        //查询数据库中所有组织名称
        List<ChainCertCaTenant> allOrgIds = chainCertCaTenantService.getAllOrgIds();
//        allOrgIds.forEach(chainCertCaTenant -> {System.out.println(chainCertCaTenant.getCaOrgId());});
        //将数据库中所有组织名称与查询到的组织名称进行比较，如果链签名CA中没有查询到的组织名称，则将数据库中该组织的状态修改为不能投票状态
        List<ChainCertCaTenant> chainCertCaTenants = allOrgIds.stream().filter(tenant -> {
            return !caList.contains(tenant.getCaOrgId().trim());
        }).collect(Collectors.toList());

        //更新数据库表中组织的状态
        chainCertCaTenantService.bathUpdateChainCertCaTenant(chainCertCaTenants);
        // 检查一下链上有的CA组织，而数据库中没有的
        List<String> caOrgIdList = allOrgIds.stream().map(tent -> {return tent.getCaOrgId().trim();}).collect(Collectors.toList());
        List<String> noCaList = caList.stream().filter(caOrgId -> {
            return !caOrgIdList.contains(caOrgId);
        }).collect(Collectors.toList());
        log.info("链上有的CA组织，而数据库中没有的,共 {}",noCaList.size());
//        noCaList.forEach(caOrgId -> {System.out.println(caOrgId);});
        chainCertCaTenantService.bathUpdateChainCertCaTenantSuccess(noCaList);
        return handleOK();
    }

    /**
     * 修改链配置中签名规则，指定只要4个指定联盟组织即可签名认证组织
     * @param chainId
     * @return
     */
    @PostMapping("/config/permission/update")
    public Object modifyTrustRootRule(@RequestParam(defaultValue = "aeotradechain", required = false) String chainId,
                                      @RequestParam(defaultValue = "CONTRACT_MANAGE-INIT_CONTRACT", required = false) String resource) {
        ChainClient aeotradechain = chainTransactionService.client("10010", chainId);
        return handleResult(chainTransactionService.modifyOfChainConfigPermissionUpdate(aeotradechain,resource));
    }

    /**
     * 查询链配置的资源权限列表
     * @param chainId
     * @return
     */
    @PostMapping("/config/permission/query")
    public Object queryChainConfigPermission(@RequestParam(defaultValue = "chain1", required = false) String chainId){
        ChainClient chain1 = chainTransactionService.client("10010", chainId);
        Object chainConfigPermissionList = chainTransactionService.getChainConfigPermissionList(chain1);
        return handleResult(chainConfigPermissionList);
    }

    @PostMapping("/config")
    public Object queryChainConfig(@RequestParam(defaultValue = "chain1", required = false) String chainId) throws ChainClientException, ChainMakerCryptoSuiteException {
        ChainClient chain1 = chainTransactionService.client("10010", chainId);
        ChainConfigOuterClass.ChainConfig chainConfig = chain1.getChainConfig(100000l);
        return handleResult(chainConfig.toString());
    }

    /**
     * 初始化ChainClient
     * @param did DID标识
     */
    @GetMapping("/did/client")
    public Object didClient(String did) {
        try {
            chainTransactionService.getChainClientByDid(did);
        } catch (Exception e) {
            return handleFail(e);
        }
        return handleOK();
    }
}

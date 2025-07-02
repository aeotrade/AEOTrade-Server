package com.aeotrade.server.chain.controller;

import com.aeotrade.chainmaker.model.*;
import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.did.contract.DidContractService;
import com.aeotrade.server.did.repository.MongoClazzRepository;
import com.aeotrade.server.did.utils.CertCheck;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson2.JSONObject;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.sdk.ChainClient;
import org.chainmaker.sdk.ChainClientException;
import org.chainmaker.sdk.SdkException;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DidController extends BaseController {
    @Value("${hmtx.chain.did.contract-admin-did:}")
    private String contractAdminDid;
    private final DidContractService didContractService;
    private final MongoClazzRepository mongoClazzRepository;
    private final ChainTransactionService chainTransactionService;

    public DidController(DidContractService didContractService, MongoClazzRepository mongoClazzRepository, ChainTransactionService chainTransactionService) {
        this.didContractService = didContractService;
        this.mongoClazzRepository = mongoClazzRepository;
        this.chainTransactionService = chainTransactionService;
    }

    private ChainClient getContractAdminForDid(String adminDid) {
        ChainClient chainClient = null;
        try {
            chainClient = chainTransactionService.getChainClientByDid(adminDid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (chainClient == null){
            throw new RuntimeException("Please initialize did ChainClient");
        }
        return chainClient;
    }

    /**
     * 设置DID合约管理员
     * 备注：aeotradeuser.tls.aeotrade是aeotradechain的创建者
     * @param chainCertUserMemberId
     * @param adminDid
     * @return
     * @throws SdkException
     */
    @GetMapping("/did/admin/set")
    public Object setAdminOfDidContract(Long chainCertUserMemberId,String adminDid) throws SdkException {
        ChainCertUserMember chainCertUserMember = mongoClazzRepository.findEntityById(ChainCertUserMember.class, "_id", chainCertUserMemberId);
        if (chainCertUserMember != null && chainCertUserMember.getSignCertId()!=null){
            ChainSdkCert sdkCert = mongoClazzRepository.findEntityById(ChainSdkCert.class, "_id", chainCertUserMember.getSignCertId());
            if (sdkCert!=null){
                return didContractService.setAdminForDidContract(getContractAdminForDid(adminDid), sdkCert.getCert());
            }
        }
        return "error";
    }
    @GetMapping("/did/admin/auth")
    public Object isAdminOfDidContract(Long chainCertUserMemberId) throws SdkException {
        ChainCertUserMember chainCertUserMember = mongoClazzRepository.findEntityById(ChainCertUserMember.class, "_id", chainCertUserMemberId);
        if (chainCertUserMember != null && chainCertUserMember.getSignCertId() != null) {
            ChainSdkCert sdkCert = mongoClazzRepository.findEntityById(ChainSdkCert.class, "_id", chainCertUserMember.getSignCertId());
            if (sdkCert != null) {
                return didContractService.isAdminOfDidContract(getContractAdminForDid(contractAdminDid), sdkCert.getCert());
            }
        }
        return false;
    }
    @GetMapping("/did/contract")
    public Object getContractInfo(String contract) throws ChainClientException, ChainMakerCryptoSuiteException {
        ContractOuterClass.Contract contractInfo = getContractAdminForDid(contractAdminDid).getContractInfo(contract, 300000L);
        return handleResult(contractInfo.toString());
    }

    /**
     * 获取链上的DID方法
     */
    @GetMapping("/did/method")
    public Object getDidMethod(String adminDid) {
        return didContractService.getDidMethodFromChain(getContractAdminForDid(adminDid));
    }
    /**
     * 设置颁发机构
     * @param issuer 被设置的颁发机构DID
     */
    @PostMapping("/did/issuer")
    public Object addIssuerToChain(String issuer,String adminDid){
        String result = didContractService.addTrustIssuerListToChain(getContractAdminForDid(contractAdminDid), List.of(new String[]{issuer}));
        return handleResult(result);
    }

    /**
     * 验证DID是否有效
     * @param did
     * @param adminDid
     * @return
     */
    @PostMapping("/did/valid")
    public Object isValidDidOnChain(String did,String adminDid){
        boolean result = didContractService.isValidDidOnChain(getContractAdminForDid(adminDid), did);
        return handleResult(result);
    }

    /**
     * 查询签发者列表
     * @param adminDid
     * @param didSearch
     * @param start
     * @param count
     */
    @GetMapping("/did/issuer/list")
    public Object getTrustIssuerListFromChain(String adminDid,String didSearch, Integer start, Integer count){
        List<String> result = didContractService.getTrustIssuerListFromChain(getContractAdminForDid(adminDid), didSearch, start, count);
        return handleResult(result);
    }
    /**
     * 设置颁发认证模板
     */
    @PostMapping("/did/vcTemplate")
    public Object addVcTemplateToChain(@RequestBody JSONObject params){
//        String adminDid,String id,String name,String version,JSONObject template
        String result = didContractService.addVcTemplateToChain(getContractAdminForDid(params.getString("adminDid")),
                params.getString("id"), params.getString("name"), params.getString("version"), params.getJSONObject("template"));
        return handleResult(result);
    }

    /**
     * 获取VC模板列表
     * @param adminDid
     * @param nameSearch
     * @param start
     * @param count
     */
    @PostMapping("/did/vcTemplate/list")
    public Object getVcTemplateListFromChain(String adminDid,String nameSearch, Integer start, Integer count){
        String result = didContractService.getVcTemplateListFromChain(getContractAdminForDid(adminDid), nameSearch, start, count);
        return handleResult(result);
    }
    /**
     * 查询VC日志
     * @param adminDid
     * @param vcIdSearch
     * @param start
     * @param count
     */
    @GetMapping("/did/vc/issueLog")
    public Object getVcIssueLogListFromChain(String adminDid, String vcIdSearch, Integer start, Integer count){
        String result = didContractService.getVcIssueLogListFromChain(getContractAdminForDid(adminDid), vcIdSearch, start, count);
        return handleResult(result);
    }

    /**
     * 在链上吊销VC
     */
    @GetMapping("/did/vc/revoke")
    public void revokeVCOnChain(String adminDid,@RequestParam(required = true) String vcId) throws SdkException {
        didContractService.revokeVCOnChain(getContractAdminForDid(adminDid), vcId);
    }

    /**
     * 从链上获取VC吊销列表
     * @param vcIdSearch vcId
     * @param start
     * @param count
     */
    @GetMapping("/did/vc/revokeList")
    public Object getVCRevokedListFromChain(String adminDid,@RequestParam(required = false) String vcIdSearch,@RequestParam(defaultValue = "0") Integer start,@RequestParam(defaultValue = "30") Integer count) throws SdkException {
        return didContractService.getVCRevokedListFromChain(getContractAdminForDid(adminDid), vcIdSearch, start, count);
    }

    /**
     * 查询当前企业下的VC列表
     * @param memberId 企业标识
     */
    //@GetMapping("/did/vc/list")
    public Object getVcListByDid(@RequestParam(required = true) String memberId){
        List<JSONObject> list = new ArrayList<>();
        Query query = new Query(Criteria.where("memberId").is(Long.parseLong(memberId)));
        List<ChainUserDids> chainUserDids = mongoClazzRepository.getMongoTemplate().find(query, ChainUserDids.class);
        for (ChainUserDids chainUserDid : chainUserDids){
            for (ChainDid chainDid : chainUserDid.getDids()){
//                if (chainDid.getDid().equals(did)){
                    List<ChainCredential> chainCredentials = chainDid.getCredentials();
                    List<JSONObject> vcList = chainCredentials.stream().map(ChainCredential::getVc).collect(Collectors.toList());
                    for (JSONObject vcJsonObject : vcList){
                        List<String> vcRevokedList = didContractService.getVCRevokedListFromChain(getContractAdminForDid(contractAdminDid), vcJsonObject.getString("id"), 0, 0);
                        if (vcRevokedList.isEmpty()){
                            vcJsonObject.put("status","revoke");
                            list.add(vcJsonObject);
                        }else {
                            vcJsonObject.put("status","normal");
                            Date expirationDate = vcJsonObject.getDate("expirationDate");
                            if (expirationDate!=null&&expirationDate.getTime()<=System.currentTimeMillis()){
                                vcJsonObject.put("status","loseEfficacy");
                            }
                            list.add(vcJsonObject);
                        }
                    }

//                }
            }
        }
        return handleResult(JSONObject.toJSONString(list));
    }
    /**
     * 下载PEM格式证书的私钥key
     *
     * @param chainSdkCertId PEM 格式的私钥标识
     */
    @PostMapping("/key/download")
    public ResponseEntity<byte[]> downloadTxtFile(String chainSdkCertId)
            throws IOException {
        if (StringUtils.isEmpty(chainSdkCertId)) {
            throw new RuntimeException("request body param : chainSdkCertId is empty");
        }
        // 获取文件内容
        String ecPrivateKey = getECPrivateKey(Long.valueOf(chainSdkCertId));

        // 设置 HTTP 头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        // 设置 Content-Disposition，允许用户自定义文件名
        headers.setContentDispositionFormData("attachment", chainSdkCertId + ".txt");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(ecPrivateKey.getBytes());
    }

    /**
     * 显示PEM格式证书的私钥key
     *
     * @param chainSdkCertId PEM 格式的私钥标识
     */
    @PostMapping("/key/show")
    public RespResult<String> showECPrivateKey(String chainSdkCertId) throws IOException {
        if (StringUtils.isEmpty(chainSdkCertId)) {
            throw new RuntimeException("request body param : chainSdkCertId is empty");
        }
        return handleResult(getECPrivateKey(Long.valueOf(chainSdkCertId)));
    }

    private String getECPrivateKey(Long chainSdkCertId) throws IOException {
        ChainSdkCert sdkCert = mongoClazzRepository.findEntityById(ChainSdkCert.class, "_id", chainSdkCertId);
        byte[] fileContent = sdkCert.getPrivateKey().getBytes();
        if (fileContent.length == 0) {
            throw new RuntimeException("fileContent is empty");
        }
        return CertCheck.extractECPrivateKey(sdkCert.getPrivateKey());
    }
}

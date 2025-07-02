package com.aeotrade.server.chain;

import com.aeotrade.chainmaker.chainclient.AeotradeChainManager;
import com.aeotrade.chainmaker.chainclient.ChainClientManager;
import com.aeotrade.chainmaker.chainclient.SdkNode;
import com.aeotrade.chainmaker.chainclient.SdkUser;
import com.aeotrade.chainmaker.constant.UserTypeEnum;
import com.aeotrade.chainmaker.exception.AeoChainException;
import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertNodeTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertNodeTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.server.chain.constant.RoleCodeRulesEnum;
import com.aeotrade.utlis.CommonUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.chainmaker.pb.accesscontrol.PolicyOuterClass;
import org.chainmaker.pb.common.ChainmakerTransaction;
import org.chainmaker.pb.common.ContractOuterClass;
import org.chainmaker.pb.common.Request;
import org.chainmaker.pb.common.ResultOuterClass;
import org.chainmaker.pb.config.ChainConfigOuterClass;
import org.chainmaker.sdk.*;
import org.chainmaker.sdk.config.*;
import org.chainmaker.sdk.crypto.ChainMakerCryptoSuiteException;
import org.chainmaker.sdk.utils.SdkUtils;
import org.chainmaker.sdk.utils.UtilsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChainTransactionService {
    private static final Logger log = LoggerFactory.getLogger(ChainTransactionService.class);
    // MAJORITY 过半；ANY 任一
    @Value("${hmtx.chain.contract-vote-rule:majority}")
    private String voteRule;
    @Autowired
    private ChainSdkCertMapper chainSdkCertService;
    @Autowired
    private ChainCertCaTenantMapper chainCertCaTenantService;
    @Autowired
    private ChainCertNodeTenantMapper chainCertNodeTenantService;
    @Autowired
    private ChainCertUserMemberMapper chainCertUserMemberService;
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 创建合约上链
     * @param contractName 合约名
     * @param version 版本号
     * @param runtimeType 合约运行环境
     * @param params 合约初始化参数
     * @param byteCodes 合约字节数组
     * @return
     */
    public Integer createContrant(String userId,String chainId, String contractName, String version, ContractOuterClass.RuntimeType runtimeType,
                                  Map<String,byte[]> params,byte[] byteCodes){

        try {
            return createUserContract(this.client(userId,chainId),majorityUserArray(),contractName,version,runtimeType,params,byteCodes);
        } catch (ChainMakerCryptoSuiteException e) {
            return 99;
        } catch (UtilsException e) {
            return 99;
        } catch (ChainClientException e) {
            return 99;
        }

    }

    public Integer upgradeContrant(String userId,String chainId, String contractName, String version, ContractOuterClass.RuntimeType runtimeType,
                                  Map<String,byte[]> params,byte[] byteCodes){

        try {
            return upgradeContract(this.client(userId,chainId),majorityUserArray(),contractName,version,runtimeType,params,byteCodes);
        } catch (ChainMakerCryptoSuiteException e) {
            return 99;
        } catch (UtilsException e) {
            return 99;
        } catch (ChainClientException e) {
            return 99;
        } catch (SdkException e) {
            return 99;
        }

    }

    /**
     * 查询所有的合约名单，包括系统合约和用户合约
     */
    public List<ContractOuterClass.Contract> contrantlist(String userId, String chainId) throws Exception {
        try {
            ChainClient client = this.client(userId, chainId);
            ContractOuterClass.Contract[] contractList = client.getContractList(10000l);
            if (contractList!=null){
                return Arrays.stream(contractList).filter(contract -> !contract.getRuntimeType().toString().equals("NATIVE")).collect(Collectors.toList());
            }
        }catch (Exception e){
            throw e;
        }
        return null;
    }

    /**
     * 数据上链
     * @param userId 会员ID
     * @param contractName 合约名称
     * @param method 合约方法
     * @param params 数据
     */
    public void addChainData(String userId,String contractName, String method,Map<String, byte[]> params,String chainId) throws AeoChainException{
        try {
            this.transaction(this.client(userId,chainId),contractName,method,null,params);
        } catch (ChainClientException e) {
            throw new AeoChainException("Chain Client Exception : "+e);
        } catch (ChainMakerCryptoSuiteException e) {
            throw new AeoChainException("ChainMaker CryptoSuite Exception : "+e);
        }
    }

    /**
     * 组织证书链上身份鉴权
     * tenantId 为 null 时，获取type类型为 00 平台的 证书信息，USER 随机获取一个平台用户证书使用
     * @param chainCertCaTenant 企业证书数据
     * @throws AeoChainException
     */
    public ChainClient addCa(String userId, ChainCertCaTenant chainCertCaTenant, String chainId) throws AeoChainException {
        // 链上身份鉴权使用的是平台定义的组织参数
        ChainCertCaTenant pingtaiCa = chainCertCaTenantService.findTop1ByTenantTypeAndIsDel( RoleCodeRulesEnum.PINGTAI.getCode(),false);
        if (pingtaiCa==null)
            throw new AeoChainException("ping-tai tenantId value is null");

        //平台系统定义的用户
        ChainCertUserMember pingtaiChainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndIsDel(pingtaiCa.getTenantId(),false);
        if (pingtaiChainCertUserMember==null)
            throw new AeoChainException("ping-tai user value is null");

        ChainSdkCert chainSdkCert = chainSdkCertService.findById(chainCertCaTenant.getCertId()).get();

        try {
            ChainClient chainClient=this.client(pingtaiChainCertUserMember.getUserId(),chainId,pingtaiChainCertUserMember);
            this.trustRootAdd(chainClient,chainSdkCert.getOrgId(), chainSdkCert.getCert(), majorityUserArray());
            return chainClient;
        } catch (ChainClientException e) {
            throw new AeoChainException("Chain Client Exception : "+e);
        } catch (ChainMakerCryptoSuiteException e) {
            throw new AeoChainException("ChainMaker CryptoSuite Exception : "+e);
        } catch (UtilsException e) {
            throw new AeoChainException("Utils Exception : "+e);
        }
    }
    public void delCa(String userId,ChainCertCaTenant chainCertCaTenant,String chainId) throws AeoChainException {
        // 链上身份鉴权使用的是平台定义的组织参数
        ChainCertCaTenant pingtaiCa = chainCertCaTenantService.findTop1ByTenantTypeAndIsDel(RoleCodeRulesEnum.PINGTAI.getCode(),false);
        if (pingtaiCa==null)
            throw new AeoChainException("ping-tai tenantId value is null");

        //平台系统定义的用户
        ChainCertUserMember pingtaiChainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndIsDel(pingtaiCa.getTenantId(),false);
        if (pingtaiChainCertUserMember==null)
            throw new AeoChainException("ping-tai user value is null");

        ChainSdkCert chainSdkCert = chainSdkCertService.findById(chainCertCaTenant.getCertId()).get();

        try {
            this.trustRootDel(this.client(userId,chainId,pingtaiChainCertUserMember),chainSdkCert.getOrgId(),
                    majorityUserArray(chainCertCaTenant.getTenantId()));
        } catch (ChainClientException e) {
            throw new AeoChainException("Chain Client Exception : "+e);
        } catch (ChainMakerCryptoSuiteException e) {
            throw new AeoChainException("ChainMaker CryptoSuite Exception : "+e);
        } catch (UtilsException e) {
            throw new AeoChainException("Utils Exception : "+e);
        }
    }

    /**
     * 获取一个组织的管理员角色的用户
     */
    public User getUserCert(String tenantId){
        ChainCertUserMember chainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndIsDel(tenantId,false);
        if (null==chainCertUserMember){
            return null;
        }
        ChainSdkCert signCert = chainSdkCertService.findById(chainCertUserMember.getSignCertId()).get();
        if (null==signCert){
            return null;
        }
        ChainSdkCert tlsCert = chainSdkCertService.findById(chainCertUserMember.getTlsCertId()).get();
        if (null==tlsCert){
            return null;
        }
        try {
            return new User(signCert.getOrgId(), signCert.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    signCert.getCert().getBytes(StandardCharsets.UTF_8),
                    tlsCert.getPrivateKey().getBytes(StandardCharsets.UTF_8),
                    tlsCert.getCert().getBytes(StandardCharsets.UTF_8));
        } catch (ChainMakerCryptoSuiteException e) {
            log.warn(e.getMessage());
            return null;
        }

    }

    /**
     * 过半数以上的各组织管理员
     */
    public User[] majorityUserArray(){
        // 创建Query对象
        Query query = new Query();
        // 指定按照_id字段进行升序排序
        query.with(Sort.by(Sort.Direction.ASC, "_id"));

        List<ChainCertCaTenant> chainCertCaTenantList ;
        if (voteRule.equalsIgnoreCase("any")){
            //任一管理者投票
            // 添加条件
            query.addCriteria(Criteria.where("isVote").is(true));
            query.addCriteria(Criteria.where("isConsensus").is(true));
            query.addCriteria(Criteria.where("isChainAuth").is(true));
            query.addCriteria(Criteria.where("isDel").is(false));
            // 只取前limit条记录
            query.limit(1);

            // 执行查询
            chainCertCaTenantList = mongoTemplate.find(query, ChainCertCaTenant.class);
//            chainCertCaTenantList = chainCertCaTenantService.findTop1ByIsVoteAndIsDelAndIsChainAuth(true,false,true);
        } else if (isNumeric(voteRule)) {
            // 添加条件
            query.addCriteria(Criteria.where("isVote").is(true));
            query.addCriteria(Criteria.where("isChainAuth").is(true));
            query.addCriteria(Criteria.where("isDel").is(false));
            // 只取前limit条记录
            query.limit(Integer.parseInt(voteRule));

            // 执行查询
            chainCertCaTenantList = mongoTemplate.find(query, ChainCertCaTenant.class);
        } else {
            //过半数以上的管理者投票
            // 添加条件
            query.addCriteria(Criteria.where("isVote").is(true));
            query.addCriteria(Criteria.where("isChainAuth").is(true));
            query.addCriteria(Criteria.where("isDel").is(false));
            // 执行查询
            long caNum = mongoTemplate.count(query, ChainCertCaTenant.class);
            // 只取前limit条记录
            query.limit(Math.toIntExact(caNum / 2 + 1));
            // 执行查询
            chainCertCaTenantList = mongoTemplate.find(query, ChainCertCaTenant.class);
        }

        List<User> userList=new ArrayList<>();
        for (ChainCertCaTenant chainCertCaTenant : chainCertCaTenantList) {
            User userCert = getUserCert(chainCertCaTenant.getTenantId());
            if(userCert!=null){
                userList.add(userCert);
            }
        }
        log.info("投票人数:{}",userList.size());
        return userList.toArray(new User[userList.size()]);
    }
    public User[] majorityUserArray(String delTentid){
        //多少个投票组织，过半数以上的管理者角色数组
        List<ChainCertCaTenant> chainCertCaTenantList = chainCertCaTenantService.findByIsVoteAndIsDel(true,false);
        List<User> userList = chainCertCaTenantList.stream().limit(chainCertCaTenantList.size()/2+2).map(ca->
                getUserCert(ca.getTenantId())).collect(Collectors.toList());
        return userList.toArray(new User[userList.size()]);
    }
    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        boolean hasDecimal = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i == 0 && c == '-') {
                continue; // 允许负号在开头
            }
            if (c == '.') {
                if (hasDecimal) {
                    return false; // 只能有一个小数点
                }
                hasDecimal = true;
            } else if (!Character.isDigit(c)) {
                return false; // 非数字字符
            }
        }
        return true;
    }
    /**
     * 全部组织管理员
     * @return
     */
    public User[] allUserArray(){
        List<ChainCertCaTenant> chainCertCaTenantList = chainCertCaTenantService.findByIsVoteAndIsDel(true,false);

        List<User> userList = chainCertCaTenantList.stream().map(ca->
                getUserCert(ca.getTenantId())).collect(Collectors.toList());
        return userList.toArray(new User[userList.size()]);
    }
    public ChainClient client(String userId,String chainId) throws AeoChainException{
        // 链上身份鉴权使用的是平台定义的组织参数
        ChainCertCaTenant pingtaiCa = chainCertCaTenantService.findTop1ByTenantTypeAndIsDel(RoleCodeRulesEnum.PINGTAI.getCode(),false);

        if (pingtaiCa==null)
            throw new AeoChainException("ping-tai tenantId value is null");
        //平台系统定义的用户
        ChainCertUserMember pingtaiChainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndIsDel(pingtaiCa.getTenantId(),false);

        if (pingtaiChainCertUserMember==null)
            throw new AeoChainException("ping-tai user value is null");
        return this.client(userId,chainId,pingtaiChainCertUserMember);
    }
    public ChainClient userClient(String userId,String tenantId,String chainId ,String uscc) throws AeoChainException{
        // 链上身份鉴权使用的是平台定义的组织参数
//        ChainCertCaTenant pingtaiCa = chainCertCaTenantService.getOne(
//                Wrappers.<ChainCertCaTenant>lambdaQuery().eq(ChainCertCaTenant::getTenantType, RoleCodeRulesEnum.PINGTAI.getCode())
//                        .eq(ChainCertCaTenant::getIsDel, false).last("LIMIT 1"));
//        if (pingtaiCa==null)
//            throw new AeoChainException("ping-tai tenantId value is null");
        //平台系统定义的用户
        ChainCertUserMember userChainCertUserMember =null;
        if(CommonUtil.isNotEmpty(uscc)&&!uscc.equals("null")){
            userChainCertUserMember=chainCertUserMemberService.findTop1ByUsccAndCertTypeAndIsDel(uscc, UserTypeEnum.ADMIN.getIndex(),false);
        }else {
            userChainCertUserMember = chainCertUserMemberService.findTop1ByTenantIdAndUserIdAndIsDel(tenantId, userId, false);
        }

        if (userChainCertUserMember==null)
            throw new AeoChainException("client user value is null");
        userId=userChainCertUserMember.getUserId();
        return this.client(userId,chainId,userChainCertUserMember);
    }
    public ChainClient client(String userId,String chainId,ChainCertUserMember chainCertUserMember) throws AeoChainException{
        if (chainCertUserMember==null)
            throw new AeoChainException("userid not have chain role");
        ChainCertCaTenant chainCertCaTenant = chainCertCaTenantService.findTop1ByTenantIdAndIsDel(chainCertUserMember.getTenantId(),false);

        List<ChainCertNodeTenant> chainCertNodeTenantList = chainCertNodeTenantService.findByTenantIdAndChainIdAndIsDel(
                chainCertCaTenant.getTenantId(),chainCertCaTenant.getChainId(),false);

        if (CollectionUtils.isEmpty(chainCertNodeTenantList)){
            chainCertNodeTenantList = chainCertNodeTenantService.findByIsShareAndChainIdAndIsDel(true,chainId,false);
        }

        SdkUser sdkUser = new SdkUser();
        ChainSdkCert signCert = chainSdkCertService.findById(chainCertUserMember.getSignCertId()).get();
        sdkUser.setSignKey(signCert.getPrivateKey().getBytes(StandardCharsets.UTF_8));
        sdkUser.setSignCert(signCert.getCert().getBytes(StandardCharsets.UTF_8));
        ChainSdkCert tlsCert = chainSdkCertService.findById(chainCertUserMember.getTlsCertId()).get();
        sdkUser.setTlsKey(tlsCert.getPrivateKey().getBytes(StandardCharsets.UTF_8));
        sdkUser.setTlsCert(tlsCert.getCert().getBytes(StandardCharsets.UTF_8));

        List<SdkNode> sdkNodeList = chainCertNodeTenantList.stream().map(node->{
            ChainSdkCert caCert = chainSdkCertService.findById(node.getCertId()).get();
            return new SdkNode(node.getNodeAddr(), caCert.getCert().getBytes(StandardCharsets.UTF_8),node.getTlsHostName(),node.getEnableTls(),node.getConnCnt());
        }).collect(Collectors.toList());

        return ChainClientManager.getInstance().getChainClient(chainId+userId+chainCertCaTenant.getTenantId(),chainCertCaTenant.getChainId(),chainCertCaTenant.getCaOrgId(),sdkNodeList,sdkUser);
    }

    /**
     * 根据did获取client
     * @param did DID标识
     */
    public ChainClient getChainClientByDid(String did) throws Exception {
        ChainCertUserMember chainCertUserMember=new ChainCertUserMember();
        chainCertUserMember.setDid(did);
        List<ChainCertUserMember> chainCertUserMemberList = chainCertUserMemberService.findAll(Example.of(chainCertUserMember));
        if(chainCertUserMemberList.isEmpty()){
            log.warn("{} 签名，未查到该机构DID标识",did);
            throw new AeoChainException("client DID value is null");
        }
        ChainCertUserMember userMember = chainCertUserMemberList.get(0);
        ChainCertCaTenant caTenant = chainCertCaTenantService.findTop1ByTenantIdAndIsDel(userMember.getTenantId(), false);
        Objects.requireNonNull(caTenant);
        Optional<ChainSdkCert> signCert = chainSdkCertService.findById(userMember.getSignCertId());
        signCert.orElseThrow(()->new AeoChainException("client signCert value is null"));
        Optional<ChainSdkCert> tlsCert = chainSdkCertService.findById(userMember.getTlsCertId());
        tlsCert.orElseThrow(()->new AeoChainException("client tlsCert value is null"));
        Optional<ChainSdkCert> ca = chainSdkCertService.findById(caTenant.getCertId());
        ca.orElseThrow(()->new AeoChainException("client ca value is null"));
        List<ChainCertNodeTenant> chainCertNodeTenantList = chainCertNodeTenantService.findByIsShareAndChainIdAndIsDel(true, caTenant.getChainId(), false);
        if (CollectionUtils.isEmpty(chainCertNodeTenantList)) {
            log.warn("{} 链，未查到节点配置",caTenant.getChainId());
            throw new AeoChainException("client Node is null");
        }
        ChainCertNodeTenant chainCertNodeTenant = chainCertNodeTenantList.get(0);
        return client(did,caTenant.getCaOrgId(),ca.get().getCert(),tlsCert.get().getPrivateKey(),tlsCert.get().getCert(),
                signCert.get().getPrivateKey(),signCert.get().getCert(),chainCertNodeTenant);
    }

    private ChainClient client(String userid,String orgId,String orgCertPem,String clientTlsKeyPem,String clientTlsCertPem,
                              String clientKeyPem,String clientCertPem,ChainCertNodeTenant chainCertNodeTenant) {
//        String ORG1_CERT_PATH="-----BEGIN CERTIFICATE-----\n" +
//                "MIICwjCCAmegAwIBAgIDApFXMAoGCCqGSM49BAMCMIGIMQswCQYDVQQGEwJDTjEQ\n" +
//                "MA4GA1UECBMHQmVpamluZzEQMA4GA1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVv\n" +
//                "LW9yZzEuYWVvdHJhZGUuY29tMRIwEAYDVQQLEwlyb290LWNlcnQxITAfBgNVBAMT\n" +
//                "GGNhLmFlby1vcmcxLmFlb3RyYWRlLmNvbTAeFw0yMjA1MTcwNjA3MjVaFw0zMjA1\n" +
//                "MTQwNjA3MjVaMIGIMQswCQYDVQQGEwJDTjEQMA4GA1UECBMHQmVpamluZzEQMA4G\n" +
//                "A1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVvLW9yZzEuYWVvdHJhZGUuY29tMRIw\n" +
//                "EAYDVQQLEwlyb290LWNlcnQxITAfBgNVBAMTGGNhLmFlby1vcmcxLmFlb3RyYWRl\n" +
//                "LmNvbTBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABN77zJgyAfjfxMMGIrrDQ+zu\n" +
//                "EbhaiSL5zpyLfioyw35rL1gvSBJj9O2L3F4S4L/ar18fo56uRs9NaIE7fV+5z8Oj\n" +
//                "gb0wgbowDgYDVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wKQYDVR0OBCIE\n" +
//                "IJisu2wQfZvZN1y3TUAj41KvbdcJFjLxuY9UpDkhSwBzMGwGA1UdEQRlMGOCDGFl\n" +
//                "b3RyYWRlLmNvbYIJbG9jYWxob3N0ghhjYS5hZW8tb3JnMS5hZW90cmFkZS5jb22H\n" +
//                "BH8AAAGHBMCoAOSHBMCoAXSHBMCoAe6HBMCoAWeHBMCoABWHBMCoAMiHBMCoAMkw\n" +
//                "CgYIKoZIzj0EAwIDSQAwRgIhAPdjXDYODOiLknsNIXsA236geXR2ecCJlgzenQez\n" +
//                "ErpIAiEAxTH7jzLnLvgr8Tpc2oau/3XqSu8cdy26iimY5TUT9a0=\n" +
//                "-----END CERTIFICATE-----\n";
        int MAX_MESSAGE_SIZE =10;
        byte[][] tlsCaCerts = new byte[0][];

        tlsCaCerts = new byte[][]{orgCertPem.getBytes(StandardCharsets.UTF_8)};


        SdkConfig sdkConfig = new SdkConfig();
        ChainClientConfig chainClientConfig = new ChainClientConfig();
        sdkConfig.setChainClient(chainClientConfig);

        RpcClientConfig rpcClientConfig = new RpcClientConfig();
        rpcClientConfig.setMaxReceiveMessageSize(MAX_MESSAGE_SIZE);

        ArchiveConfig archiveConfig = new ArchiveConfig();
        archiveConfig.setDest("localhost:3306");
        archiveConfig.setType("mysql");
        archiveConfig.setSecretKey("xxx");

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.setTrustRootBytes(tlsCaCerts);
        nodeConfig.setTlsHostName(chainCertNodeTenant.getTlsHostName());
        nodeConfig.setEnableTls(chainCertNodeTenant.getEnableTls());
        nodeConfig.setNodeAddr(chainCertNodeTenant.getNodeAddr());
        nodeConfig.setConn_cnt(chainCertNodeTenant.getConnCnt());

        NodeConfig[] nodeConfigs = new NodeConfig[]{nodeConfig};

        AeotradeChainManager chainManager = AeotradeChainManager.getInstance();
        ChainClient chainClient = chainManager.getChainClient(userid);

        chainClientConfig.setOrgId(orgId);
        chainClientConfig.setChainId(chainCertNodeTenant.getChainId());
//        String CLIENT1_TLS_KEY_PATH  = "-----BEGIN EC PRIVATE KEY-----\n" +
//                "MHcCAQEEIM5gdETXH4AFb38aIN64sNtxN7i+0xfEeuUVxZiTRpaJoAoGCCqGSM49\n" +
//                "AwEHoUQDQgAEwRr1JWeHiwwjMjwg13DZ5/LrMevGdhacZ2hMyIkVvmaJX4ZnIQRc\n" +
//                "lU5F9VQoFnWDK4ibl9COb9CWjovRfJHPeg==\n" +
//                "-----END EC PRIVATE KEY-----\n";
//        String CLIENT1_TLS_CERT_PATH  = "-----BEGIN CERTIFICATE-----\n" +
//                "MIIChTCCAiygAwIBAgIDDTrvMAoGCCqGSM49BAMCMIGIMQswCQYDVQQGEwJDTjEQ\n" +
//                "MA4GA1UECBMHQmVpamluZzEQMA4GA1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVv\n" +
//                "LW9yZzEuYWVvdHJhZGUuY29tMRIwEAYDVQQLEwlyb290LWNlcnQxITAfBgNVBAMT\n" +
//                "GGNhLmFlby1vcmcxLmFlb3RyYWRlLmNvbTAeFw0yMjA1MTcwNjA3MjVaFw0yNzA1\n" +
//                "MTYwNjA3MjVaMIGMMQswCQYDVQQGEwJDTjEQMA4GA1UECBMHQmVpamluZzEQMA4G\n" +
//                "A1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVvLW9yZzEuYWVvdHJhZGUuY29tMQ4w\n" +
//                "DAYDVQQLEwVhZG1pbjEpMCcGA1UEAxMgYWRtaW4xLnRscy5hZW8tb3JnMS5hZW90\n" +
//                "cmFkZS5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATBGvUlZ4eLDCMyPCDX\n" +
//                "cNnn8usx68Z2FpxnaEzIiRW+ZolfhmchBFyVTkX1VCgWdYMriJuX0I5v0JaOi9F8\n" +
//                "kc96o38wfTAOBgNVHQ8BAf8EBAMCA/gwEwYDVR0lBAwwCgYIKwYBBQUHAwIwKQYD\n" +
//                "VR0OBCIEIEzOLQTBe2l9X7PztlzxkkgYnP5UspPQrQnu/w6HMRkwMCsGA1UdIwQk\n" +
//                "MCKAIJisu2wQfZvZN1y3TUAj41KvbdcJFjLxuY9UpDkhSwBzMAoGCCqGSM49BAMC\n" +
//                "A0cAMEQCIALeEKwcvJ0NDpPvwPYUBc6XHKSijOLV0meGeQP2rNopAiBuVjF/NRly\n" +
//                "blCDxr/+8YxU3rvjZ/Bfx0+1k4H4Nm3t+Q==\n" +
//                "-----END CERTIFICATE-----\n";
//        String CLIENT1_KEY_PATH  = "-----BEGIN EC PRIVATE KEY-----\n" +
//                "MHcCAQEEID0Ce+cbFzcgm6bjIh16RA75fcjD9a18lViG3phgOMbAoAoGCCqGSM49\n" +
//                "AwEHoUQDQgAEHd+aSNW5nNFy/ont3fXHzVC2sKhe2ndNgaEfsmoZNgpEN0USoqJl\n" +
//                "xNh2E3uvvyd0z/Q20tP+pZJe8uwKQwjKvA==\n" +
//                "-----END EC PRIVATE KEY-----\n";
//        String CLIENT1_CERT_PATH  = "-----BEGIN CERTIFICATE-----\n" +
//                "MIICcTCCAhigAwIBAgIDBJsEMAoGCCqGSM49BAMCMIGIMQswCQYDVQQGEwJDTjEQ\n" +
//                "MA4GA1UECBMHQmVpamluZzEQMA4GA1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVv\n" +
//                "LW9yZzEuYWVvdHJhZGUuY29tMRIwEAYDVQQLEwlyb290LWNlcnQxITAfBgNVBAMT\n" +
//                "GGNhLmFlby1vcmcxLmFlb3RyYWRlLmNvbTAeFw0yMjA1MTcwNjA3MjVaFw0yNzA1\n" +
//                "MTYwNjA3MjVaMIGNMQswCQYDVQQGEwJDTjEQMA4GA1UECBMHQmVpamluZzEQMA4G\n" +
//                "A1UEBxMHQmVpamluZzEeMBwGA1UEChMVYWVvLW9yZzEuYWVvdHJhZGUuY29tMQ4w\n" +
//                "DAYDVQQLEwVhZG1pbjEqMCgGA1UEAxMhYWRtaW4xLnNpZ24uYWVvLW9yZzEuYWVv\n" +
//                "dHJhZGUuY29tMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHd+aSNW5nNFy/ont\n" +
//                "3fXHzVC2sKhe2ndNgaEfsmoZNgpEN0USoqJlxNh2E3uvvyd0z/Q20tP+pZJe8uwK\n" +
//                "QwjKvKNqMGgwDgYDVR0PAQH/BAQDAgbAMCkGA1UdDgQiBCCrfYC/SB6lPPPICbTV\n" +
//                "wVWblaiXesjL0tJ6XX5li4UuoDArBgNVHSMEJDAigCCYrLtsEH2b2Tdct01AI+NS\n" +
//                "r23XCRYy8bmPVKQ5IUsAczAKBggqhkjOPQQDAgNHADBEAiBx+hLzB0PHuTzeqzh0\n" +
//                "5QOfosF6h0SPgn6suDz9OGUUhQIgfEgBcR37XUZ147PVxLxgcnO7+/G+PQp3q4PO\n" +
//                "ZZWDxCo=\n" +
//                "-----END CERTIFICATE-----\n";
        chainClientConfig.setUserKeyBytes(clientTlsKeyPem.getBytes(StandardCharsets.UTF_8));
        chainClientConfig.setUserCrtBytes(clientTlsCertPem.getBytes(StandardCharsets.UTF_8));
        chainClientConfig.setUserSignKeyBytes(clientKeyPem.getBytes(StandardCharsets.UTF_8));
        chainClientConfig.setUserSignCrtBytes(clientCertPem.getBytes(StandardCharsets.UTF_8));
        chainClientConfig.setRpcClient(rpcClientConfig);
        chainClientConfig.setArchive(archiveConfig);
        chainClientConfig.setNodes(nodeConfigs);

        if (chainClient == null) {
            try {
                chainClient = chainManager.createChainClient(userid,sdkConfig);
            } catch (ChainClientException e) {
                throw new RuntimeException(e);
            } catch (RpcServiceClientException e) {
                throw new RuntimeException(e);
            } catch (UtilsException e) {
                throw new RuntimeException(e);
            } catch (ChainMakerCryptoSuiteException e) {
                throw new RuntimeException(e);
            }
        }
        return chainClient;
    }

    //根据txid 查询链上数据
    public ChainmakerTransaction.Transaction getTxById(ChainClient client,String txId) throws ChainClientException, ChainMakerCryptoSuiteException {
        ChainmakerTransaction.TransactionInfo transactionInfo = client.getTxByTxId(txId,10000l);
        if (transactionInfo.hasTransaction()) {
            return transactionInfo.getTransaction();
        }
        return null;
    }
    public String tongbuca(ChainClient client) {

        List<ChainCertCaTenant> chainCertCaTenantList = chainCertCaTenantService.findAll();
        List<String> orglist = chainConfigOrgIdList(client);
        for (ChainCertCaTenant ca:chainCertCaTenantList){
            if (orglist.stream().anyMatch(o->o.equals(ca.getCaOrgId()))){
                ca.setIsVote(true);
                ca.setIsChainAuth(true);
                ca.setIsDel(false);
                chainCertCaTenantService.save(ca);
            }else {
                ca.setIsVote(false);
                ca.setIsChainAuth(false);
                ca.setIsDel(true);
                chainCertCaTenantService.save(ca);
                //
                List<ChainCertUserMember> chainCertUserMemberList=chainCertUserMemberService.findByTenantId(ca.getTenantId());
                chainCertUserMemberList.stream().forEach(m->chainCertUserMemberService.save(m.setIsDel(false)));
            }
        }
        return "success";
    }
    // 向链上发送交易
    public String transaction(ChainClient client,String contractName, String method, String txId, Map<String, byte[]> params)
            throws ChainClientException, ChainMakerCryptoSuiteException {
        ResultOuterClass.TxResponse txResponse = client.invokeContract(contractName, method,
                txId, params, 10000, 10000);
        //新添加的组织，第一次提交交易有可能失败，可能是等大部分节点都同步完该组织证书的信息，调用才能成功
        if (txResponse.getCodeValue()>0){
            throw new AeoChainException(txResponse.getMessage());
        }
        return txResponse.getTxId();
    }
    //对 CA证书进行链上身份认证
    public String trustRootAdd(ChainClient client,String orgId,String ca,User[] users)
            throws ChainClientException, ChainMakerCryptoSuiteException, UtilsException {
        String[] certList = new String[]{ca};

        Request.Payload payload = client.createPayloadOfChainConfigTrustRootAdd(
                orgId, certList, 10000);
        Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, users);
        ResultOuterClass.TxResponse responseInfo = client.sendContractManageRequest(
                payload, endorsementEntries, 10000, 10000);
        if (responseInfo.getCodeValue()>0){
            throw new AeoChainException(responseInfo.getMessage());
        }
        return responseInfo.getMessage();
    }
    public String trustRootDel(ChainClient client,String orgId,User[] users)
            throws ChainClientException, ChainMakerCryptoSuiteException, UtilsException {
        Request.Payload payload = client.createPayloadOfChainConfigTrustRootDelete(orgId,10000);
        Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, users);
        ResultOuterClass.TxResponse responseInfo = client.sendContractManageRequest(
                payload, endorsementEntries, 10000, 10000);

        if (responseInfo.getCodeValue()>0){
            throw new AeoChainException(responseInfo.getMessage());
        }
        return responseInfo.getMessage();
    }

    /**
     * 创建用户合约
     * @param client
     * @param users
     * @param contractName 合约名
     * @param version 版本号
     * @param runtimeType 合约运行环境
     * @param params 合约初始化参数
     * @param byteCode 合约字节数组
     */
    public Integer createUserContract(ChainClient client,User[] users,String contractName,String version,
                                   ContractOuterClass.RuntimeType runtimeType,Map<String, byte[]> params,byte[] byteCode)
            throws ChainMakerCryptoSuiteException, UtilsException, ChainClientException {
        // 1. create payload
        Request.Payload payload = client.createContractCreatePayload(contractName, version, byteCode,
                runtimeType, params);
        //2. create payloads with endorsement
        Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, users);
        // 3. send request
        ResultOuterClass.TxResponse responseInfo = client.sendContractManageRequest(payload, endorsementEntries, 10000, 10000);
        return responseInfo.getContractResult().getCode();
    }
    public Integer upgradeContract(ChainClient client,User[] users,String contractName,String version,
                                      ContractOuterClass.RuntimeType runtimeType,Map<String, byte[]> params,byte[] byteCode)
                                    throws SdkException{
        ResultOuterClass.TxResponse responseInfo = null;
        try {

            // 1. create payload
            Request.Payload payload = client.createContractUpgradePayload(contractName, version, byteCode,
                    runtimeType, params);

            //2. create payloads with endorsement
            Request.EndorsementEntry[] endorsementEntries = SdkUtils.getEndorsers(payload, users);

            // 3. send request
            responseInfo = client.sendContractManageRequest(payload, endorsementEntries, 10000, 10000);
        } catch (SdkException e) {
            throw e;
        }
        return responseInfo.getContractResult().getCode();
    }

    public String modifyOfChainConfigPermissionUpdate(ChainClient caLient,String permissionResourceName){
        ResultOuterClass.TxResponse responseInfo = null;
        Request.Payload payload = null;
        try {
            Iterable<String> iterable=new ArrayList<>();
            PolicyOuterClass.Policy defaultInstance = PolicyOuterClass.Policy.newBuilder()
                    .addAllOrgList(iterable)
                    // 将规则设置为整数4，代表有任意4个组织签名即可
                    .setRule("4")
                    .addRoleList("admin")
                    .buildPartial();
            payload = caLient.createPayloadOfChainConfigPermissionUpdate(permissionResourceName,
                    defaultInstance, 10000);
            //2. create payloads with endorsement
            Request.EndorsementEntry[] endorsementEntries = SdkUtils
                    .getEndorsers(payload, majorityUserArray());

            // 3. send request
            responseInfo = caLient.updateChainConfig(payload, endorsementEntries, 20000, 20000);

            if (responseInfo.getCode() ==  ResultOuterClass.TxStatusCode.SUCCESS){
                if (responseInfo.hasContractResult()) {
                    log.info("VC模板上链： {}",responseInfo.getContractResult().getResult().toStringUtf8());
                    return responseInfo.getContractResult().getResult().toStringUtf8();
                }
            }

        } catch (SdkException e) {
            log.warn(e.getMessage());
        }
        return "no message";
    }
    public Object getChainConfigPermissionList(ChainClient caLient){
        try {
            ChainConfigOuterClass.ChainConfig chainConfig = caLient.getChainConfig(10000);
            List<ChainConfigOuterClass.ResourcePolicy> policiesList = chainConfig.getResourcePoliciesList();
            List<Map<String, Object>> mapList = policiesList.stream().map(policy -> {
                Map<String, Object> map = new HashMap<>();
                map.put("resourceName", policy.getResourceName());
                map.put("rule", policy.getPolicy().getRule());
                map.put("roleList", policy.getPolicy().getRoleListList());
                return map;
            }).collect(Collectors.toList());
            log.info("链配置权限列表：{}",mapList);
            return mapList;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return e.getMessage();
        }
    }
    private Boolean getChainConfigOrgIds(ChainClient chainClient,String orgId) {
        List<String> list=chainConfigOrgIdList(chainClient);
        if (list.stream().anyMatch(c->c.equals(orgId))){
            return true;
        }
        return false;
    }

    private List<String> chainConfigOrgIdList(ChainClient chainClient) {
        ChainConfigOuterClass.ChainConfig chainConfig = null;
        try {
            chainConfig = chainClient.getChainConfig(10000);
        } catch (SdkException e) {
            log.warn(e.getMessage());
        }
        return chainConfig.getTrustRootsList().stream().map(c->c.getOrgId()).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        try {
            new ChainTransactionService().getTxById(null,"e43d3a306c5542f092f409d908a6b4a00975e1e71dd8469c9750aa9d5d0b0a98");
        } catch (ChainClientException e) {
            throw new RuntimeException(e);
        } catch (ChainMakerCryptoSuiteException e) {
            throw new RuntimeException(e);
        }
    }
}

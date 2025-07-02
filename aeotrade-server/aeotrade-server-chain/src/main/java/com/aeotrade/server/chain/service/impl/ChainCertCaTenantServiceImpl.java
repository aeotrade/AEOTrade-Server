package com.aeotrade.server.chain.service.impl;

import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.server.chain.service.IChainCertCaTenantService;
import com.aeotrade.server.chain.vo.ChainCertCaTenantVo;
import com.aeotrade.server.chain.vo.ChainCertUserMemberVO;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 企业租户证书管理 服务实现类
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@Service
public class ChainCertCaTenantServiceImpl implements IChainCertCaTenantService {
    private static final Logger log = LoggerFactory.getLogger(ChainCertCaTenantServiceImpl.class);
    @Autowired
    private ChainCertCaTenantMapper chainCertCaTenantMapper;
    @Autowired
    private ChainCertUserMemberMapper chainCertUserMemberMapper;
    @Autowired
    private ChainSdkCertMapper chainSdkCertMapper;
    @Value("${http.apiurl:https://api1.aeotrade.com/uac/uac/manager/by/uscc}")
    private String url;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public ChainCertCaTenantVo findById(String memberId, String staffId, Integer pageNo, Integer pageSize,Integer isAdmin) {
        /**查询企业证书*/
        ChainCertCaTenantVo chainCertCaTenantVo = new ChainCertCaTenantVo();
        ChainCertCaTenant chainCertCaTenant = chainCertCaTenantMapper.findTop1ByTenantIdAndIsDel(memberId, false);
        if (null == chainCertCaTenant) {
            return chainCertCaTenantVo;
        }
        ChainSdkCert chainSdkCert = chainSdkCertMapper.findTop1ByOrgIdAndIsDel(chainCertCaTenant.getCaOrgId(), false);
        chainCertCaTenantVo.setChainCertCaTenant(chainCertCaTenant);
        chainCertCaTenantVo.setChainSdkCertId(chainSdkCert.getId().toString());
        chainCertCaTenantVo.setCert(chainSdkCert.getCert());
        chainCertCaTenantVo.setPrivateKey(chainSdkCert.getPrivateKey());
        chainCertCaTenantVo.setIsChainAuth(chainCertCaTenant.getIsChainAuth());
        /**查询员工信息*/
        List<ChainCertUserMemberVO> chainCertUserMemberVOList = new ArrayList<>();
        if (isAdmin==1) {
            //是主管理员的时候查询全部员工
            ChainCertUserMember chainCertUserMember = new ChainCertUserMember();
            chainCertUserMember.setTenantId(memberId);
//            chainCertUserMember.setCertType(4);
            if (pageNo <= 0) {
                pageNo = 1;
            }
            pageNo = pageNo - 1;
            if (pageSize <= 0) {
                pageSize = 10;
            }
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                    .withMatcher("isDel", ExampleMatcher.GenericPropertyMatchers.contains()) //姓名采用“开始匹配”的方式查询
                    .withMatcher("tenantId", ExampleMatcher.GenericPropertyMatchers.contains()) //姓名采用“开始匹配”的方式查询
                    .withMatcher("certType", ExampleMatcher.GenericPropertyMatchers.contains()) //姓名采用“开始匹配”的方式查询
                    .withIgnorePaths("id","uscc");  //忽略属性：是否关注。因为是基本类型，需要忽略掉
            Example<ChainCertUserMember> example = Example.of(chainCertUserMember, matcher);
            org.springframework.data.domain.Page<ChainCertUserMember> all = chainCertUserMemberMapper.findAll(example, pageable);
           /* org.springframework.data.domain.Page<Map<String, Object>>  mapPage = chainCertUserMemberMapper.selectMapsPage(new Page<>(pageNo, pageSize),
                    Wrappers.<ChainCertUserMember>lambdaQuery()
                    .eq(ChainCertUserMember::getTenantId, memberId)
                    .eq(ChainCertUserMember::getIsDel, false));*/
            chainCertCaTenantVo.setTotal(all.getTotalElements());
            for (ChainCertUserMember certUserMember : all.getContent()) {
                ChainCertUserMemberVO chainCertUserMemberVO = new ChainCertUserMemberVO();
                //查询员工证书
                ChainSdkCert signCert = chainSdkCertMapper.findById((Long) certUserMember.getSignCertId()).get();
                ChainSdkCert tlsCert = chainSdkCertMapper.findById((Long) certUserMember.getTlsCertId()).get();
                Map<String, String> map = new HashMap<>();
                map.put("signcert", signCert.getCert());
                map.put("signprivatekey", signCert.getPrivateKey());
                map.put("tlscert", tlsCert.getCert());
                map.put("tlsprivatekey", tlsCert.getPrivateKey());
                //使用DID合约生成的标识
                chainCertUserMemberVO.setDid(certUserMember.getDid());
                //员工用户链上身份id
                chainCertUserMemberVO.setOrgId(signCert.getCertName());
                //员工用户创建时间
                chainCertUserMemberVO.setCreatTime((Date) certUserMember.getCreateAt());
                //员工用户类型
                chainCertUserMemberVO.setUserType(String.valueOf(certUserMember.getUserType()));
                //员工用户证书
                chainCertUserMemberVO.setStringStringMap(map);
                chainCertUserMemberVOList.add(chainCertUserMemberVO);
            }
        } else {
            ChainCertUserMember chainCertUserMember = new ChainCertUserMember();
            chainCertUserMember.setTenantId(memberId);
            chainCertUserMember.setUserId(staffId);
            if (pageNo <= 0) {
                pageNo = 1;
            }
            pageNo = pageNo - 1;
            if (pageSize <= 0) {
                pageSize = 10;
            }
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                    .withMatcher("tenantId", ExampleMatcher.GenericPropertyMatchers.contains())
                    .withMatcher("userId", ExampleMatcher.GenericPropertyMatchers.contains())
                    .withMatcher("isDel", ExampleMatcher.GenericPropertyMatchers.contains())
                    .withIgnorePaths("id","uscc");  //忽略属性：是否关注。因为是基本类型，需要忽略掉
            Example<ChainCertUserMember> example = Example.of(chainCertUserMember, matcher);
            org.springframework.data.domain.Page<ChainCertUserMember> all = chainCertUserMemberMapper.findAll(example, pageable);
            chainCertCaTenantVo.setTotal(all.getTotalElements());
            for (ChainCertUserMember certUserMember : all.getContent()) {
                ChainCertUserMemberVO chainCertUserMemberVO = new ChainCertUserMemberVO();
                //查询员工证书
                ChainSdkCert signCert = chainSdkCertMapper.findById((Long) certUserMember.getSignCertId()).get();
                ChainSdkCert tlsCert = chainSdkCertMapper.findById((Long) certUserMember.getTlsCertId()).get();
                Map<String, String> map = new HashMap<>();
                map.put("signcert", signCert.getCert());
                map.put("signprivatekey", signCert.getPrivateKey());
                map.put("tlscert", tlsCert.getCert());
                map.put("tlsprivatekey", tlsCert.getPrivateKey());
                //使用DID合约生成的标识
                chainCertUserMemberVO.setDid(certUserMember.getDid());
                //员工用户链上身份id
                chainCertUserMemberVO.setOrgId(signCert.getCertName());
                //员工用户创建时间
                chainCertUserMemberVO.setCreatTime((Date) certUserMember.getCreateAt());
                //员工用户类型
                chainCertUserMemberVO.setUserType(String.valueOf(certUserMember.getUserType()));
                //员工用户证书
                chainCertUserMemberVO.setStringStringMap(map);
                chainCertUserMemberVOList.add(chainCertUserMemberVO);
            }
        }
        chainCertCaTenantVo.setChainCertUserMemberVOS(chainCertUserMemberVOList);
        return chainCertCaTenantVo;
    }

    @Override
    public ChainCertCaTenant getById(String id) {
        return chainCertCaTenantMapper.findTop1ByTenantIdAndIsDel(id,false);
    }

    @Override
    public Boolean findByRobotId(String memberId) {
        ChainCertUserMember byTenantIdAndUserType = chainCertUserMemberMapper.findTop1ByTenantIdAndUserType(memberId,"机器人");
        if(null!=byTenantIdAndUserType){
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Object> getByUscc(String uscc) throws Exception {
        Map<String, Object> map=new HashMap<>();
        ChainCertCaTenant top1ByUsccAndIsDel = chainCertCaTenantMapper.findTop1ByUsccAndIsDel(uscc, false);
        if(null==top1ByUsccAndIsDel){
            Map<String, Object> usccmap=new HashMap<>();
            usccmap.put("uscc",uscc);
            String string = HttpRequestUtils.httpGet(url, usccmap);
            RespResult respResult = JSONObject.parseObject(string, RespResult.class);
            List<Document> documents = JSONObject.parseArray(String.valueOf(respResult.getResult()), Document.class);
            map.put("org_id",documents.get(0).get("id"));
            map.put("chain_id","");
            return map;
        }
        map.put("org_id",top1ByUsccAndIsDel.getTenantId());
        map.put("chain_id",top1ByUsccAndIsDel.getCaOrgId());
        return map;
    }

    @Override
    public List<ChainCertCaTenant> getAllOrgIds() {
        Query query=new Query();
        query.addCriteria(Criteria.where("isDel").is(false));
        query.addCriteria(Criteria.where("isVote").is(true));
        query.fields().include("caOrgId");
        return mongoTemplate.find(query, ChainCertCaTenant.class);
    }

    public void bathUpdateChainCertCaTenantSuccess(List<String> chainCertCaTenantList) {
        if (chainCertCaTenantList.isEmpty()){
            log.info("batch update chain cert ca tenant is empty");
            return;
        }

        Query query = new Query();
        //查询条件
        query.addCriteria(Criteria.where("caOrgId").in(chainCertCaTenantList));

        Update update = new Update();
        //更新内容
        update.set("isVote", true);
        update.set("isChainAuth", true);
        update.set("isWhether", false);
        update.set("isDel",false);
        UpdateResult result = mongoTemplate.updateMulti(query, update, ChainCertCaTenant.class);

        log.info("Synchronization chain CA organization success status: {}",result.getModifiedCount());
    }

    @Override
    public void bathUpdateChainCertCaTenant(List<ChainCertCaTenant> chainCertCaTenantList) {
        if (chainCertCaTenantList.isEmpty()){
            log.info("batch update chain cert ca tenant is empty");
            return;
        }
//        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ChainCertCaTenant.class);

        Query query = new Query();
        //查询条件
        query.addCriteria(Criteria.where("caOrgId").in(chainCertCaTenantList.stream().map(ChainCertCaTenant::getCaOrgId).collect(Collectors.toList())));

        Update update = new Update();
        //更新内容
        update.set("isVote", false);
        update.set("isChainAuth", false);
        update.set("isWhether", true);
        UpdateResult result = mongoTemplate.updateMulti(query, update, ChainCertCaTenant.class);
//        bulkOperations.updateMulti(query, update);
//        BulkWriteResult result = bulkOperations.execute();

        log.info("Synchronization chain CA organization status: {}",result.getModifiedCount());
    }

    public void updateChainCertCaTenant(ChainCertCaTenant chainCertCaTenant) {

        Query query = new Query();
        //查询条件
        query.addCriteria(Criteria.where("_id").is(chainCertCaTenant.getId()));

        Update update = new Update();
        //更新内容
        update.set("isVote", false);
        update.set("isChainAuth", false);
        update.set("isWhether", true);

        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ChainCertCaTenant.class);
        log.info("Synchronization chain CA organization status: {}",updateResult.getModifiedCount());
    }
}

package com.aeotrade.server.chain.agreement;

import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.tradeagreement.contract.TradeAgreementContractService;
import com.aeotrade.server.tradeagreement.domain.CircleMemberVO;
import com.aeotrade.server.tradeagreement.entity.AgrCircle;
import com.aeotrade.server.tradeagreement.entity.AgrCircleMembers;
import com.aeotrade.server.tradeagreement.entity.AgrMatch;
import com.aeotrade.server.tradeagreement.entity.AgrMatchInfo;
import com.aeotrade.server.tradeagreement.service.IAgrCircleMembersService;
import com.aeotrade.server.tradeagreement.service.IAgrCircleService;
import com.aeotrade.server.tradeagreement.service.IAgrMatchInfoService;
import com.aeotrade.server.tradeagreement.service.IAgrMatchService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 圈子定时任务
 */
@Slf4j
@Component
public class SyncDataTask {
    @Value("${hmtx.agreement.sync-data-user-did:}")
    private String syncDDataUserDid;
    @Value("${hmtx.agreement.dify-server.url:}")
    private String difyServerUrl;
    @Value("${hmtx.agreement.dify-server.match-api-key:}")
    private String difyServerMatchApiKey;
    @Value("${hmtx.agreement.dify-server.response-mode:blocking}")
    private String difyServerResponseMode;
    @Value("${hmtx.agreement.dify-server.user:test-user-id-001}")
    private String difyServerUser;

    private final TradeAgreementContractService tradeAgreementContractService;
    private final ChainTransactionService chainTransactionService;
    private final RestTemplate difyRestTemplate;
    private final IAgrCircleService agrCircleService;
    private final IAgrCircleMembersService agrCircleMembersService;
    private final IAgrMatchInfoService agrMatchInfoService;
    private final IAgrMatchService agrMatchService;
    private final AlibabaOssTool alibabaOssTool;

    public SyncDataTask(TradeAgreementContractService tradeAgreementContractService, ChainTransactionService chainTransactionService,
                        RestTemplate difyRestTemplate, IAgrCircleService agrCircleService, IAgrCircleMembersService agrCircleMembersService,
                        IAgrMatchInfoService agrMatchInfoService, IAgrMatchService agrMatchService, AlibabaOssTool alibabaOssTool) {
        this.tradeAgreementContractService = tradeAgreementContractService;
        this.chainTransactionService = chainTransactionService;
        this.difyRestTemplate = difyRestTemplate;
        this.agrCircleService = agrCircleService;
        this.agrCircleMembersService = agrCircleMembersService;
        this.agrMatchInfoService = agrMatchInfoService;
        this.agrMatchService = agrMatchService;
        this.alibabaOssTool = alibabaOssTool;
    }

    private ChainClient getContractAdminForDid(String adminDid) {
        ChainClient chainClient = null;
        try {
            chainClient = chainTransactionService.getChainClientByDid(adminDid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (chainClient == null) {
            throw new RuntimeException("Please initialize did ChainClient");
        }
        return chainClient;
    }

    /**
     * 同步圈子数据
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void syncCircleData() {
        if (syncDDataUserDid == null || syncDDataUserDid.isEmpty()) {
            return;
        }
        JSONArray allCircles = tradeAgreementContractService.getAllCircles(getContractAdminForDid(syncDDataUserDid), 1L, 10000L);
        if (allCircles == null || allCircles.isEmpty()) {
            return;
        }
        log.info("allCircles: {}", allCircles);
        // 圈子数据落地
        // 删除链上没有的圈子
        // 获取所有需要排除的 circleId 列表
        List<String> circleIdsToExclude = allCircles.stream()
                .filter(circle -> circle instanceof JSONObject)
                .map(circle -> (JSONObject) circle)
                .map(json -> json.getString("id"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        removeOutdatedCircles(circleIdsToExclude);
        // 保存链上有本地没有的圈子数据
        allCircles.forEach(chainData -> {
            JSONObject json = (JSONObject) chainData;
            if (agrCircleService.lambdaQuery().eq(AgrCircle::getCircleId, json.getString("id")).exists()) {
                return;
            }
            AgrCircle circle = new AgrCircle();
            circle.setName(json.getString("name"));
            circle.setType((byte) 0);
            circle.setCreator(json.getString("creatorDID"));
            circle.setCreatorOrgName(json.getString("creatorOrgName"));
            circle.setOwner(json.getString("ownerDID"));
            circle.setStatus((byte) json.getIntValue("disabled"));
            circle.setParentId(0);
            circle.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
            circle.setCreatedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(json.getLongValue("creationTime")), ZoneId.systemDefault()));
            circle.setCircleId(json.getString("id"));
            circle.setOwnerOrgName(json.getString("ownerOrgName"));
            circle.setDescription(json.getString("description"));
            agrCircleService.save(circle);
            //保存圈子成员-创建者
            if (!agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, circle.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, circle.getCreator()).exists()) {
                AgrCircleMembers circleMembers = new AgrCircleMembers();
                circleMembers.setCircleId(circle.getCircleId());
                circleMembers.setMemberDid(circle.getCreator());
                circleMembers.setRole((byte) 0);
                circleMembers.setStatus((byte) 1);
                circleMembers.setOrgName(circle.getCreatorOrgName());

                agrCircleMembersService.save(circleMembers);
            }
            //保存圈子成员-圈子拥有者
            if (!agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, circle.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, circle.getOwner()).exists()) {
                AgrCircleMembers circleMembers = new AgrCircleMembers();
                circleMembers.setCircleId(circle.getCircleId());
                circleMembers.setMemberDid(circle.getOwner());
                circleMembers.setRole((byte) 1);
                circleMembers.setStatus((byte) 1);
                if (circle.getOwner().equals(circle.getCreator())) {
                    circleMembers.setOrgName(circle.getCreatorOrgName());
                }
                agrCircleMembersService.save(circleMembers);
            }
        });

    }

    /**
     * 同步圈子成员数据
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void syncCircleMembersData() {
        if (syncDDataUserDid == null || syncDDataUserDid.isEmpty()) {
            return;
        }
        agrCircleService.lambdaQuery().list().forEach(circle -> {
            JSONArray circleMembers = tradeAgreementContractService.getCircleMembers(getContractAdminForDid(syncDDataUserDid), circle.getCircleId(), 1L, 10000L);
            if (circleMembers == null || circleMembers.isEmpty()) {
                return;
            }
            log.info("circleMembers: {}", circleMembers);
            circleMembers.forEach(circleMember -> {
                JSONObject json = (JSONObject) circleMember;
                List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, circle.getCircleId())
                        .eq(AgrCircleMembers::getMemberDid, json.getString("did")).list();
                if (agrCircleMembersList.isEmpty()) {
                    AgrCircleMembers newCircleMember = new AgrCircleMembers();
                    newCircleMember.setCircleId(circle.getCircleId());
                    newCircleMember.setMemberDid(json.getString("did"));
                    if (circle.getCreator().equals(newCircleMember.getMemberDid())) {
                        newCircleMember.setRole((byte) 0);
                    } else if (circle.getOwner().equals(newCircleMember.getMemberDid())) {
                        newCircleMember.setRole((byte) 1);
                    } else {
                        newCircleMember.setRole((byte) 2);
                    }
                    newCircleMember.setStatus((byte) 1);
                    newCircleMember.setOrgName(json.getString("orgName"));
                    if (json.get("socialCreditCode") != null) {
                        newCircleMember.setUscc(json.getString("socialCreditCode"));
                    }
                    agrCircleMembersService.save(newCircleMember);
                } else {
                    if (agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, circle.getCircleId())
                            .eq(AgrCircleMembers::getMemberDid, json.getString("did"))
                            .eq(AgrCircleMembers::getStatus, (byte) 0).exists()) {
                        agrCircleMembersService.lambdaUpdate().eq(AgrCircleMembers::getCircleId, circle.getCircleId())
                                .eq(AgrCircleMembers::getMemberDid, json.getString("did"))
                                .set(AgrCircleMembers::getStatus, (byte) 1).update();
                    }
                }
            });
        });

    }

    /**
     * 同步申请状态
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void syncApproveJoinCircleStatus() {
        if (syncDDataUserDid == null || syncDDataUserDid.isEmpty()) {
            return;
        }

        agrCircleService.lambdaQuery().list().forEach(agrCircle -> {
            JSONArray applications = tradeAgreementContractService.getApplications(getContractAdminForDid(syncDDataUserDid), agrCircle.getCircleId(), 1L, 10000L);
            if (applications == null || applications.isEmpty()) {
                return;
            }
            applications.forEach(application -> {
                JSONObject applicationJson = (JSONObject) application;
                if (applicationJson.getString("status").equals("2")) {
                    agrCircleMembersService.lambdaUpdate().eq(AgrCircleMembers::getCircleId, agrCircle.getCircleId()).eq(AgrCircleMembers::getMemberDid, applicationJson.getString("applicantDID")).remove();
                }
            });

        });
    }

    /**
     * 同步 需求/产品 内容数据
     * 注意：
     * type = 0 需求;1 产品
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void syncRequirementAndProductData() {
        if (syncDDataUserDid == null || syncDDataUserDid.isEmpty()) {
            return;
        }
        JSONArray allProducts = tradeAgreementContractService.getAllProducts(getContractAdminForDid(syncDDataUserDid), 1L, 10000L);
        log.info("AllProducts: size {} content {}", allProducts.size(), allProducts);
        if (allProducts == null || allProducts.isEmpty()) {
            if (agrMatchInfoService.lambdaQuery().exists()) {
                agrMatchInfoService.remove(new QueryWrapper<>());
            }
            return;
        }
        //内容数据落地
        allProducts.stream().forEach(product -> {
            JSONObject productJson = (JSONObject) product;
            List<AgrMatchInfo> agrMatchInfoList = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getTxId, productJson.getString("id")).list();

            if (agrMatchInfoList.isEmpty()) {
                AgrMatchInfo agrMatchInfo = new AgrMatchInfo();
                agrMatchInfo.setTxId(productJson.getString("id"));
                agrMatchInfo.setCircleId(productJson.getString("circleId"));
                agrMatchInfo.setMemberDid(productJson.getString("ownerDID"));
                agrMatchInfo.setName(productJson.getString("name"));
                agrMatchInfo.setPrice(productJson.getString("price"));
                agrMatchInfo.setType((byte) productJson.getIntValue("productType"));
                agrMatchInfo.setDeadline(StringUtils.isEmpty(productJson.getString("validity"))?null:LocalDate.parse(productJson.getString("validity"), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59));
                agrMatchInfo.setStatus((byte) 3);
                agrMatchInfo.setIpfsHash(productJson.getString("ipfsHash"));
                agrMatchInfo.setImage(productJson.getString("image"));
                agrMatchInfo.setDescription(productJson.getString("description"));
                agrMatchInfo.setNum(productJson.getString("stock"));
                agrMatchInfo.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
                agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
                agrMatchInfo.setTypeName(productJson.getString("categoryId"));
                agrMatchInfoService.save(agrMatchInfo);
            } else {
                AgrMatchInfo agrMatchInfo = agrMatchInfoList.get(0);
                agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
                byte status = agrMatchInfo.getStatus();
                if (productJson.getString("onShelf").equals("0") && agrMatchInfo.getStatus() != 4) {
                    status = (byte) 4;
                } else if (productJson.getString("onShelf").equals("1") && agrMatchInfo.getStatus() != 3) {
                    status = (byte) 3;

                }
                if (!agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getTxId, agrMatchInfo.getTxId())
                        .eq(AgrMatchInfo::getCircleId, productJson.getString("circleId"))
                        .eq(AgrMatchInfo::getStatus, status).exists()) {
                    agrMatchInfo.setStatus((byte) 4);
                    agrMatchInfoService.updateById(agrMatchInfo);
                }
            }
        });
    }

    /**
     * 定时调用匹配数据接口
     */
    @Scheduled(fixedDelay = 1000 * 20)
    public void syncMatchData() {
        if (difyServerUrl == null || difyServerUrl.isEmpty() || difyServerMatchApiKey == null || difyServerMatchApiKey.isEmpty()) {
            return;
        }
        // 需要与产品数据匹配
        List<AgrMatchInfo> supplyList = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getType, (byte) 1)
                .eq(AgrMatchInfo::getStatus, (byte) 3).list();
        supplyList.stream().map(supply -> {
            supply.setDetail(alibabaOssTool.readUrlContent(supply.getIpfsHash()));
            return supply;
        }).collect(Collectors.toList());
        List<AgrMatchInfo> demandList = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getType, (byte) 0)
                .eq(AgrMatchInfo::getStatus, (byte) 3).gt(AgrMatchInfo::getDeadline, LocalDateTime.now()).list();
        demandList.stream().map(demand -> {
            demand.setDetail(alibabaOssTool.readUrlContent(demand.getIpfsHash()));
            return demand;
        }).collect(Collectors.toList());
        List<AgrCircle> circleList = agrCircleService.lambdaQuery().eq(AgrCircle::getStatus, (byte) 0).list();
        circleList.stream().map(circle -> {
            circle.setMembers(agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, circle.getCircleId()).list().stream().map(circleMember -> {
                CircleMemberVO circleMemberVO = new CircleMemberVO();
                circleMemberVO.setId(circleMember.getId());
                circleMemberVO.setCircleId(circleMember.getCircleId());
                circleMemberVO.setMemberDid(circleMember.getMemberDid());
                circleMemberVO.setOrgName(circleMember.getOrgName());
                return circleMemberVO;
            }).collect(Collectors.toList()));
            return circle;
        }).collect(Collectors.toList());
        //请求dify服务
        Object rs = requestDifyServer(
                JSONArray.parseArray(JSONArray.toJSONString(supplyList)),
                JSONArray.parseArray(JSONArray.toJSONString(demandList)),
                JSONArray.parseArray(JSONArray.toJSONString(circleList)));
        //将匹配结果落地
        log.info("DifyServer rs: {}", JSON.toJSONString(rs));
        JSONArray matchJsonArray = null;
        if (rs != null && JSON.isValidObject(rs.toString())) {
            JSONObject jsonObject = JSONObject.parseObject(rs.toString());
            if (jsonObject.containsKey("data")) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data!=null&&data.containsKey("outputs")) {
                    JSONObject outputs = data.getJSONObject("outputs");
                    if (outputs!=null&&outputs.containsKey("match_json")) {
                        try {
                            String matchJson = outputs.getString("match_json");
                            matchJsonArray = JSONArray.parseArray(matchJson);
                        } catch (Exception e) {
                            log.warn(e.getMessage());
                        }

                    }
                }
            }
        }
        if (matchJsonArray != null && !matchJsonArray.isEmpty()) {
            matchJsonArray.forEach(matchJson -> {
                JSONObject matchJsonObject = (JSONObject) matchJson;
                String supplyId = matchJsonObject.getString("supply_id");
                String demandId = matchJsonObject.getString("demand_id");
                int matchScore = matchJsonObject.getIntValue("match_score");
                String matchReason = matchJsonObject.getString("reason");
                if (!agrMatchService.lambdaQuery().eq(AgrMatch::getDemandTxId, demandId)
                        .eq(AgrMatch::getSupplyTxId, supplyId).exists()) {
                    List<AgrMatchInfo> supplyMatchInfoList = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getTxId, supplyId).list();
                    if (supplyMatchInfoList.isEmpty()) {
                        return;
                    }
                    AgrMatchInfo supplyMatchInfo = supplyMatchInfoList.get(0);
                    List<AgrMatchInfo> demandMatchInfoList = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getTxId, demandId).list();
                    if (demandMatchInfoList.isEmpty()) {
                        return;
                    }
                    AgrMatchInfo demandMatchInfo = demandMatchInfoList.get(0);

                    AgrMatch agrMatch = new AgrMatch();
                    agrMatch.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
                    agrMatch.setDemandId(demandMatchInfo.getId());
                    agrMatch.setDemandMemberDid(demandMatchInfo.getMemberDid());
                    agrMatch.setDemandName(demandMatchInfo.getName());
                    agrMatch.setDemandOrgName(demandMatchInfo.getMemberOrg());
                    agrMatch.setDemandTxId(demandMatchInfo.getTxId());
                    agrMatch.setDemandCircleId(demandMatchInfo.getCircleId());
                    List<AgrCircle> demandAgrCircleList = agrCircleService.lambdaQuery().eq(AgrCircle::getCircleId, demandMatchInfo.getCircleId()).list();
                    if (!demandAgrCircleList.isEmpty()) {
                        agrMatch.setDemandCircleName(demandAgrCircleList.get(0).getName());
                    }
                    agrMatch.setSupplyId(supplyMatchInfo.getId());
                    agrMatch.setSupplyMemberDid(supplyMatchInfo.getMemberDid());
                    agrMatch.setSupplyName(supplyMatchInfo.getName());
                    agrMatch.setSupplyOrgName(supplyMatchInfo.getMemberOrg());
                    agrMatch.setSupplyTxId(supplyMatchInfo.getTxId());
                    agrMatch.setSupplyImage(supplyMatchInfo.getImage());
                    agrMatch.setSupplyCircleId(supplyMatchInfo.getCircleId());
                    List<AgrCircle> supplyAgrCircleList = agrCircleService.lambdaQuery().eq(AgrCircle::getCircleId, supplyMatchInfo.getCircleId()).list();
                    if (supplyAgrCircleList != null) {
                        agrMatch.setDemandCircleName(supplyAgrCircleList.get(0).getName());
                    }
                    agrMatch.setMatchScore(matchScore);
                    agrMatch.setMatchReason(matchReason);

                    agrMatchService.save(agrMatch);
                }
            });
        }
    }

    /**
     * 请求dify服务
     *
     * @param supplyList 供应列表
     * @param demandList 需求列表
     * @param circleList 圈子列表
     */
    private String requestDifyServer(JSONArray supplyList, JSONArray demandList, JSONArray circleList) {
        // 准备请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.ALL));
        headers.set("Authorization", "Bearer " + difyServerMatchApiKey);
        headers.setConnection("keep-alive");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 准备请求体
        JSONObject inputsJsonObject = new JSONObject();
        inputsJsonObject.put("supply_list", supplyList.toString());
        inputsJsonObject.put("demand_list", demandList.toString());
        inputsJsonObject.put("circle_list", circleList.toString());

        JSONObject requestBodyJsonObject = new JSONObject();
        requestBodyJsonObject.put("inputs", inputsJsonObject);
        requestBodyJsonObject.put("response_mode", difyServerResponseMode);
        requestBodyJsonObject.put("user", difyServerUser);
        log.info("Dify Server Request Body: {}", requestBodyJsonObject);
        // 创建HttpEntity
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJsonObject.toString(), headers);

        // 发送POST请求
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(60000); // 设置连接超时时间为60秒
        factory.setReadTimeout(60 * 10 * 1000); // 设置读取超时时间为10分钟
        difyRestTemplate.setRequestFactory(factory);
        ResponseEntity<String> response = difyRestTemplate.postForEntity(difyServerUrl, requestEntity, String.class);

        // 打印响应状态码和响应体
        log.info("Dify Server Response Status Code: {} Body: {}", response.getStatusCode(), response.getBody());

        return response.getBody();
    }

    /**
     * 删除与链上不同步的圈子
     */
    private void removeOutdatedCircles(List<String> circleIdsToExclude) {
        List<AgrCircle> circlesToRemove = agrCircleService.lambdaQuery()
                .notIn(AgrCircle::getCircleId, circleIdsToExclude)
                .list();
        log.info("删除与链上不同步的圈子标识: {}", circlesToRemove.stream().map(AgrCircle::getCircleId).collect(Collectors.toList()));
        circlesToRemove.forEach(agrCircleService::removeById);
    }
}

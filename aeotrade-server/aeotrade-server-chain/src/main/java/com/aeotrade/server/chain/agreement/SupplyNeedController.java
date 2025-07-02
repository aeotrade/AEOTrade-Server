package com.aeotrade.server.chain.agreement;

import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.tradeagreement.contract.TradeAgreementContractService;
import com.aeotrade.server.tradeagreement.domain.MatchInfoVo;
import com.aeotrade.server.tradeagreement.domain.Product;
import com.aeotrade.server.tradeagreement.domain.Requirement;
import com.aeotrade.server.tradeagreement.entity.AgrCircle;
import com.aeotrade.server.tradeagreement.entity.AgrMatchInfo;
import com.aeotrade.server.tradeagreement.service.IAgrCircleService;
import com.aeotrade.server.tradeagreement.service.IAgrMatchInfoService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 供应方
 */
@Slf4j
@RestController
public class SupplyNeedController extends BaseController {
    @Value("${hmtx.agreement.sync-data-user-did:}")
    private String syncDDataUserDid;



    private final TradeAgreementContractService tradeAgreementContractService;
    private final ChainTransactionService chainTransactionService;
    private final IAgrMatchInfoService agrMatchInfoService;
    private final IAgrCircleService circleService;
    private final DifyTool difyTool;
    private final AlibabaOssTool alibabaOssTool;

    public SupplyNeedController(TradeAgreementContractService tradeAgreementContractService,
                                ChainTransactionService chainTransactionService, IAgrMatchInfoService agrMatchInfoService,
                                IAgrCircleService circleService, DifyTool difyTool, AlibabaOssTool alibabaOssTool) {
        this.tradeAgreementContractService = tradeAgreementContractService;
        this.chainTransactionService = chainTransactionService;
        this.agrMatchInfoService = agrMatchInfoService;
        this.circleService = circleService;
        this.difyTool = difyTool;
        this.alibabaOssTool = alibabaOssTool;
    }

    private ChainClient getContractAdminForDid(String adminDid) {
        ChainClient chainClient = null;
        try {
            chainClient = chainTransactionService.getChainClientByDid(syncDDataUserDid);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (chainClient == null){
            throw new RuntimeException("Please initialize did ChainClient");
        }
        return chainClient;
    }



    /**
     * 发布需求
     */
    @PostMapping("/requirement/create")
    public RespResult createRequirement(@Validated(Requirement.class) @RequestBody MatchInfoVo matchInfoVo){
        //内容审核
        JSONObject auditResult = difyTool.contentAudit("题目:"+matchInfoVo.getName()+"简介:"+matchInfoVo.getDescription());
        if (auditResult == null) {
            return handleFail("无法触达内容审核服务");
        } else if (auditResult.getObject("data", JSONObject.class)
                        .getObject("outputs", JSONObject.class).getBooleanValue("review_result")) {
            return handleFail(auditResult.getObject("data", JSONObject.class)
                    .getObject("outputs", JSONObject.class).getString("review_reasoning"));
        }
        //截止日期
        try {
            LocalDateTime.parse(matchInfoVo.getDeadline() + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            // 记录解析错误的日志
            log.warn("截止日期格式错误：{}", e.getMessage());
            return handleFail("请输入正确的截止日期,格式:yyyy-MM-dd");
        }

        // 数据上链
        String txId = UUID.randomUUID().toString();

        AgrMatchInfo agrMatchInfo = new AgrMatchInfo();
        agrMatchInfo.setTxId(txId);
        agrMatchInfo.setMemberDid(matchInfoVo.getMemberDID());
        agrMatchInfo.setName(matchInfoVo.getName());
        agrMatchInfo.setPrice(matchInfoVo.getPrice());
        agrMatchInfo.setType((byte) 0);// 需求
        agrMatchInfo.setDeadline(LocalDate.parse(matchInfoVo.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59));
        agrMatchInfo.setStatus((byte) 0);
        agrMatchInfo.setImage(matchInfoVo.getImage());
        agrMatchInfo.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setDescription(matchInfoVo.getDescription());
        agrMatchInfo.setTypeName(matchInfoVo.getTypeName()==null?"":matchInfoVo.getTypeName());
        agrMatchInfo.setNum(matchInfoVo.getNumber()==null?"1":matchInfoVo.getNumber());
        agrMatchInfo.setIpfsHash(matchInfoVo.getDetail()==null?"":alibabaOssTool.uploadContent(matchInfoVo.getDetail()));
        agrMatchInfoService.save(agrMatchInfo);

        return handleResult(agrMatchInfo.getId());
    }

    /**
     * 修改需求
     */
    @PostMapping("/requirement/update")
    public RespResult updateRequirement(@Validated(Requirement.class) @RequestBody MatchInfoVo requirementVo) {
        if (requirementVo.getId() == null) {
            return handleFail("请选择需求");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(requirementVo.getId());
        if (agrMatchInfo == null) {
            return handleFail("选择需求ID未查询到数据");
        }
        if (!agrMatchInfo.getMemberDid().equals(requirementVo.getMemberDID())) {
            return handleFail("无权限修改");
        }

        if (requirementVo.getDetail()!=null){
            requirementVo.setDetail(alibabaOssTool.uploadContent(requirementVo.getDetail()));
        }

        if (agrMatchInfo.getCircleId() != null) {
            tradeAgreementContractService.editProduct(getContractAdminForDid(requirementVo.getMemberDID()),
                    agrMatchInfo.getTxId(), requirementVo.getMemberDID(), requirementVo.getName(), requirementVo.getPrice(),
                    requirementVo.getDetail(), requirementVo.getDescription(),
                    requirementVo.getDeadline(), requirementVo.getNumber() == null ? "1" : requirementVo.getNumber(), requirementVo.getTypeName());
        }

        agrMatchInfo.setName(requirementVo.getName());
        agrMatchInfo.setPrice(requirementVo.getPrice());
        agrMatchInfo.setDescription(requirementVo.getDescription());
        agrMatchInfo.setIpfsHash(requirementVo.getDetail());
        agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setDeadline(LocalDate.parse(requirementVo.getDeadline(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(23, 59, 59));
        agrMatchInfoService.updateById(agrMatchInfo);

        return handleOK();

    }

    /**
     * 发布到圈子
     */
    @PostMapping("/info/publish")
    public RespResult infoPublish(@RequestBody MatchInfoVo matchInfoVo){
        if (matchInfoVo.getId() == null){
            return handleFail("请选择,或保存后再操作");
        }
        if (matchInfoVo.getCircleIds() == null || matchInfoVo.getCircleIds().isEmpty()){
            return handleFail("请选择圈子");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(matchInfoVo.getId());
        if (agrMatchInfo == null){
            return handleFail("选择需求ID未查询到数据");
        }
        if (!agrMatchInfo.getMemberDid().equals(matchInfoVo.getMemberDID())){
            return handleFail("无权限修改");
        }
        StringBuilder message = new StringBuilder();
        AgrCircle agrCircle = null;
        for (String circleId : matchInfoVo.getCircleIds()) {
            List<AgrCircle> agrCircleList = circleService.lambdaQuery().eq(AgrCircle::getCircleId, circleId).list();
            if (agrCircleList == null || agrCircleList.isEmpty()) {
                message.append("圈子标识: ").append(circleId).append(" 未找到关联数据");
                continue;
            }
            agrCircle = agrCircleList.get(0);
            Boolean createProductResult = tradeAgreementContractService.createProduct(getContractAdminForDid(matchInfoVo.getMemberDID()),
                    agrMatchInfo.getTxId(), circleId, agrMatchInfo.getMemberDid(), agrMatchInfo.getName(),
                    agrMatchInfo.getPrice(), Long.valueOf(agrMatchInfo.getType()), agrMatchInfo.getIpfsHash() != null ? agrMatchInfo.getIpfsHash() : "",
                    agrMatchInfo.getDescription()==null ? "" : agrMatchInfo.getDescription(), agrMatchInfo.getDeadline() != null ? agrMatchInfo.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "",
                    agrMatchInfo.getNum() == null ? "1" : agrMatchInfo.getNum(), agrMatchInfo.getTypeName() == null ? "" : agrMatchInfo.getTypeName());
            if (!createProductResult) {
                message.append("圈子标识: ").append(circleId).append(" 发布失败");
                continue;
            }
            //审批
            Boolean approved = tradeAgreementContractService.approveProduct(getContractAdminForDid(agrMatchInfo.getMemberDid()),
                    circleId, agrCircle.getOwner(), agrMatchInfo.getTxId());
            byte status = agrMatchInfo.getStatus();
            if (approved) {
                status = (byte) 1;
                //上架
                Boolean setProductOnShelfResult = tradeAgreementContractService.setProductOnShelf(getContractAdminForDid(agrMatchInfo.getMemberDid()),
                        agrMatchInfo.getTxId(), agrMatchInfo.getMemberDid(), true);
                if (setProductOnShelfResult) {
                    status = (byte)3;
                }
            } else {
                status = (byte)2;
            }
            if (!agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getTxId, agrMatchInfo.getTxId())
                    .eq(AgrMatchInfo::getCircleId, circleId)
                    .eq(AgrMatchInfo::getStatus, status).exists()) {
                AgrMatchInfo agrMatchInfoTemp = new AgrMatchInfo();
                agrMatchInfoTemp.setId(agrMatchInfo.getId());
                agrMatchInfoTemp.setStatus(status);
                agrMatchInfoTemp.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
                agrMatchInfoTemp.setCircleId(circleId);
                agrMatchInfoService.updateById(agrMatchInfoTemp);
            }
        }
        return message.length() == 0 ? handleOK() :handleFail(message.toString());
    }

    /**
     * 撤销发布
     */
    @PostMapping("/info/cancel")
    public RespResult infoCancel(@RequestBody MatchInfoVo matchInfoVo){
        if (matchInfoVo.getId() == null){
            return handleFail("请选择内容ID");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(matchInfoVo.getId());
        if (agrMatchInfo == null){
            return handleFail("选择需求ID未查询到数据");
        }
        if (!agrMatchInfo.getMemberDid().equals(matchInfoVo.getMemberDID())){
            return handleFail("无权限修改");
        }
        if (agrMatchInfo.getStatus() != 3){
            if (agrMatchInfo.getStatus() == 4) {
                return handleFail("该内容已经下架");
            }
            return handleFail("该内容未发布");
        }
        //下架
        Boolean setProductOnShelfResult = tradeAgreementContractService.setProductOnShelf(getContractAdminForDid(agrMatchInfo.getMemberDid()),
                agrMatchInfo.getTxId(), agrMatchInfo.getMemberDid(), false);
        if (setProductOnShelfResult) {
            AgrMatchInfo approvedAgrMatchInfo = new AgrMatchInfo();
            approvedAgrMatchInfo.setId(agrMatchInfo.getId());
            approvedAgrMatchInfo.setStatus((byte) 4);
            approvedAgrMatchInfo.setUpdatedAt(LocalDateTime.now());
            agrMatchInfoService.updateById(approvedAgrMatchInfo);
            return handleOK();
        }
        return handleFail("撤销失败");
    }

    /**
     * 需求详情
     */
    @GetMapping("/requirement/detail")
    public RespResult requirementDetail(String id){
        if (id == null){
            return handleFail("请选择需求");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(id);
        if (agrMatchInfo == null){
            return handleFail("选择需求ID未查询到数据");
        }
        MatchInfoVo matchInfoVo = new MatchInfoVo();
        matchInfoVo.setId(agrMatchInfo.getId());
        matchInfoVo.setCircleId(agrMatchInfo.getCircleId());
        matchInfoVo.setMemberDID(agrMatchInfo.getMemberDid());
        matchInfoVo.setName(agrMatchInfo.getName());
        matchInfoVo.setPrice(agrMatchInfo.getPrice());
        matchInfoVo.setDeadline(agrMatchInfo.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        matchInfoVo.setDetail(alibabaOssTool.readUrlContent(agrMatchInfo.getIpfsHash()));
        matchInfoVo.setCreatedAt(agrMatchInfo.getCreatedAt());
        matchInfoVo.setUpdatedAt(agrMatchInfo.getUpdatedAt());
        matchInfoVo.setDescription(agrMatchInfo.getDescription());
        matchInfoVo.setStatus(Integer.valueOf(agrMatchInfo.getStatus()));
        matchInfoVo.setMemberOrg(agrMatchInfo.getMemberOrg());

        return handleResult(matchInfoVo);
    }
    /**
     * 需求列表
     */
    @GetMapping("/requirement/list")
    public RespResult requirementList(String memberDID,@RequestParam(required = false) String value, @RequestParam(defaultValue = "1") Long pageNo,@RequestParam(defaultValue = "10") Long pageSize){
        com.aeotrade.server.chain.config.Page<MatchInfoVo> agrCircleMembersVOPage=new com.aeotrade.server.chain.config.Page<>();
        if(pageNo <=0){
            pageNo = 1L;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10L;
        }
        LambdaQueryWrapper<AgrMatchInfo> queryWrapper = new LambdaQueryWrapper<AgrMatchInfo>()
                .eq(AgrMatchInfo::getType, (byte) 0)
                .eq(AgrMatchInfo::getMemberDid, memberDID);

        if (!StringUtils.isEmpty(value)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(AgrMatchInfo::getName, value)
                    .or()
                    .like(AgrMatchInfo::getDescription, value));
        }
        Page<AgrMatchInfo> pageResult = agrMatchInfoService.page(
                new Page<>(pageNo, pageSize),queryWrapper);
        agrCircleMembersVOPage.setTotal(pageResult.getTotal());
        agrCircleMembersVOPage.setRecords(pageResult.getRecords().stream().map(agrMatchInfo -> {
            MatchInfoVo matchInfoVo = new MatchInfoVo();
            matchInfoVo.setId(agrMatchInfo.getId());
            matchInfoVo.setCircleId(agrMatchInfo.getCircleId());
            matchInfoVo.setMemberDID(agrMatchInfo.getMemberDid());
            matchInfoVo.setName(agrMatchInfo.getName());
            matchInfoVo.setPrice(agrMatchInfo.getPrice());
            matchInfoVo.setDeadline(agrMatchInfo.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            matchInfoVo.setCreatedAt(agrMatchInfo.getCreatedAt());
            matchInfoVo.setTxId(agrMatchInfo.getTxId());
            matchInfoVo.setStatus(Integer.valueOf(agrMatchInfo.getStatus()));
            return matchInfoVo;
        }).collect(Collectors.toList()));

        return handleResult(agrCircleMembersVOPage);
    }
    /**
     * 需求推荐
     */
    @GetMapping("/requirement/recommend")
    public RespResult recommendRequirementList(){
        return handleOK();
    }
    /**
     * 产品创建
     */
    @PostMapping("/product/create")
    public RespResult createProduct(@Validated(Product.class) @RequestBody MatchInfoVo matchInfoVo)
    {
        //内容审核
        JSONObject auditResult = difyTool.contentAudit("题目:"+matchInfoVo.getName()+"简介:"+matchInfoVo.getDescription()+"内容:"+matchInfoVo.getDetail());
        if (auditResult == null) {
            return handleFail("无法触达内容审核服务");
        } else if (auditResult.getObject("data", JSONObject.class)
                .getObject("outputs", JSONObject.class).getBooleanValue("review_result")) {
            return handleFail(auditResult.getObject("data", JSONObject.class)
                    .getObject("outputs", JSONObject.class).getString("review_reasoning"));
        }

        // 数据上链
        String txId = UUID.randomUUID().toString();
        if (!StringUtils.isEmpty(matchInfoVo.getDetail())){
            matchInfoVo.setDetail(alibabaOssTool.uploadContent(matchInfoVo.getDetail()));
        }

        AgrMatchInfo agrMatchInfo = new AgrMatchInfo();
        agrMatchInfo.setTxId(txId);
        agrMatchInfo.setMemberDid(matchInfoVo.getMemberDID());
        agrMatchInfo.setName(matchInfoVo.getName());
        agrMatchInfo.setPrice(matchInfoVo.getPrice());
        agrMatchInfo.setType((byte) 1); // 产品
        agrMatchInfo.setStatus((byte) 0);
        agrMatchInfo.setIpfsHash(matchInfoVo.getDetail());
        agrMatchInfo.setImage(matchInfoVo.getImage());
        agrMatchInfo.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setDescription(matchInfoVo.getDescription());
        agrMatchInfo.setNum(matchInfoVo.getNumber()==null?"":matchInfoVo.getNumber());
        agrMatchInfo.setTypeName(matchInfoVo.getTypeName());
        agrMatchInfoService.save(agrMatchInfo);

        return handleResult(agrMatchInfo.getId());
    }

    /**
     * 产品列表
     */
    @GetMapping("/product/list")
    public RespResult productList(String memberDID,@RequestParam(required = false) String value,
                                  @RequestParam(defaultValue = "1") Long pageNo,@RequestParam(defaultValue = "10") Long pageSize){
        com.aeotrade.server.chain.config.Page<MatchInfoVo> agrCircleMembersVOPage=new com.aeotrade.server.chain.config.Page<>();
        if(pageNo <=0){
            pageNo = 1L;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10L;
        }
        LambdaQueryWrapper<AgrMatchInfo> queryWrapper = new LambdaQueryWrapper<AgrMatchInfo>()
                .eq(AgrMatchInfo::getType, (byte) 1)
                .eq(AgrMatchInfo::getMemberDid, memberDID);

        if (!StringUtils.isEmpty(value)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(AgrMatchInfo::getName, value)
                    .or()
                    .like(AgrMatchInfo::getDescription, value));
        }
        Page<AgrMatchInfo> pageResult = agrMatchInfoService.page(
                new Page<>(pageNo, pageSize),queryWrapper);
        agrCircleMembersVOPage.setTotal(pageResult.getTotal());
        agrCircleMembersVOPage.setRecords(pageResult.getRecords().stream().map(agrMatchInfo -> {
            MatchInfoVo matchInfoVo = new MatchInfoVo();
            matchInfoVo.setId(agrMatchInfo.getId());
            matchInfoVo.setCircleId(agrMatchInfo.getCircleId());
            matchInfoVo.setMemberDID(agrMatchInfo.getMemberDid());
            matchInfoVo.setName(agrMatchInfo.getName());
            matchInfoVo.setPrice(agrMatchInfo.getPrice());
            matchInfoVo.setCreatedAt(agrMatchInfo.getCreatedAt());
            matchInfoVo.setTxId(agrMatchInfo.getTxId());
            matchInfoVo.setDetail(alibabaOssTool.readUrlContent(agrMatchInfo.getIpfsHash()));
            matchInfoVo.setStatus(Integer.valueOf(agrMatchInfo.getStatus()));
            return matchInfoVo;
        }).collect(Collectors.toList()));

        return handleResult(agrCircleMembersVOPage);
    }
    /**
     * 产品详情
     */
    @GetMapping("/product/detail")
    public RespResult productDetail(String id){
        if (id == null){
            return handleFail("请选择需求");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(id);
        if (agrMatchInfo == null){
            return handleFail("选择需求ID未查询到数据");
        }
        MatchInfoVo matchInfoVo = new MatchInfoVo();
        matchInfoVo.setTxId(agrMatchInfo.getTxId());
        matchInfoVo.setId(agrMatchInfo.getId());
        matchInfoVo.setCircleId(agrMatchInfo.getCircleId());
        matchInfoVo.setMemberDID(agrMatchInfo.getMemberDid());
        matchInfoVo.setName(agrMatchInfo.getName());
        matchInfoVo.setPrice(agrMatchInfo.getPrice());
        matchInfoVo.setDetail(alibabaOssTool.readUrlContent(agrMatchInfo.getIpfsHash()));
        matchInfoVo.setCreatedAt(agrMatchInfo.getCreatedAt());
        matchInfoVo.setUpdatedAt(agrMatchInfo.getUpdatedAt());
        matchInfoVo.setDescription(agrMatchInfo.getDescription());
        matchInfoVo.setStatus(Integer.valueOf(agrMatchInfo.getStatus()));
        matchInfoVo.setTypeName(agrMatchInfo.getTypeName());
        matchInfoVo.setMemberOrg(agrMatchInfo.getMemberOrg());

        return handleResult(matchInfoVo);
    }
    /**
     * 产品编辑
     */
    @PostMapping("/product/update")
    public RespResult productUpdate(@Validated(Product.class) @RequestBody MatchInfoVo matchInfoVo){
        if (matchInfoVo.getId() == null) {
            return handleFail("请选择");
        }
        AgrMatchInfo agrMatchInfo = agrMatchInfoService.getById(matchInfoVo.getId());
        if (agrMatchInfo == null) {
            return handleFail("选择需求ID未查询到数据");
        }
        if (!agrMatchInfo.getMemberDid().equals(matchInfoVo.getMemberDID())) {
            return handleFail("无权限修改");
        }

        if (matchInfoVo.getDetail()!=null){
            matchInfoVo.setDetail(alibabaOssTool.uploadContent(matchInfoVo.getDetail()));
        }

        if (agrMatchInfo.getCircleId() != null) {
            tradeAgreementContractService.editProduct(getContractAdminForDid(matchInfoVo.getMemberDID()),
                    agrMatchInfo.getTxId(), matchInfoVo.getMemberDID(), matchInfoVo.getName(), matchInfoVo.getPrice(),
                    matchInfoVo.getDetail() != null ? matchInfoVo.getDetail() : "", matchInfoVo.getDescription(),
                    matchInfoVo.getDeadline() == null ? "" : matchInfoVo.getDeadline(), matchInfoVo.getNumber() == null ? "1" : matchInfoVo.getNumber(), matchInfoVo.getTypeName());
        }

        agrMatchInfo.setName(matchInfoVo.getName());
        agrMatchInfo.setPrice(matchInfoVo.getPrice());
        agrMatchInfo.setDescription(matchInfoVo.getDescription());
        agrMatchInfo.setUpdatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        agrMatchInfo.setIpfsHash(matchInfoVo.getDetail() != null ? matchInfoVo.getDetail() : "");
        agrMatchInfo.setTypeName(matchInfoVo.getTypeName());
        agrMatchInfoService.updateById(agrMatchInfo);

        return handleOK();

    }

    /**
     * 上传文件
     * @param file  文件
     */
    @PostMapping("/upload")
    public RespResult handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return handleFail("请选择要上传的文件");
        }
        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")) + fileExtension;
            return handleResult(alibabaOssTool.uploadImage(file.getInputStream(), newFilename));
        } catch (Exception e) {
            return handleFail(e.getMessage());
        }
    }
}

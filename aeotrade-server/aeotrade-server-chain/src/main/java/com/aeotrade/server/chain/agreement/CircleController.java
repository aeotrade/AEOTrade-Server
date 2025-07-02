package com.aeotrade.server.chain.agreement;

import com.aeotrade.server.chain.ChainTransactionService;
import com.aeotrade.server.tradeagreement.contract.TradeAgreementContractService;
import com.aeotrade.server.tradeagreement.domain.ApplyForVo;
import com.aeotrade.server.tradeagreement.domain.CircleMemberVO;
import com.aeotrade.server.tradeagreement.domain.CircleVo;
import com.aeotrade.server.tradeagreement.entity.AgrCircle;
import com.aeotrade.server.tradeagreement.entity.AgrCircleMembers;
import com.aeotrade.server.tradeagreement.service.IAgrCircleMembersService;
import com.aeotrade.server.tradeagreement.service.IAgrCircleService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.chainmaker.sdk.ChainClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 圈子
 */
@RestController
public class CircleController extends BaseController {
    @Value("${hmtx.agreement.sync-data-user-did:}")
    private String syncDDataUserDid;
    @Value("${hmtx.agreement.recommend:}")
    private List<String> recommendCircleIds = new ArrayList<>();

    private final IAgrCircleService circleService;
    private final TradeAgreementContractService tradeAgreementContractService;
    private final ChainTransactionService chainTransactionService;
    private final IAgrCircleMembersService agrCircleMembersService;

    public CircleController(IAgrCircleService circleService, TradeAgreementContractService tradeAgreementContractService, ChainTransactionService chainTransactionService, IAgrCircleMembersService agrCircleMembersService, IAgrCircleService iAgrCircleService) {
        this.circleService = circleService;
        this.tradeAgreementContractService = tradeAgreementContractService;
        this.chainTransactionService = chainTransactionService;
        this.agrCircleMembersService = agrCircleMembersService;
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
     * 创建圈子
     */
    @PostMapping("/circle/create")
    public RespResult createCircle(@Validated @RequestBody CircleVo circleVo) {
        String circleId = circleService.getNextCircleId();
        Boolean isCircle = tradeAgreementContractService.createCircle(getContractAdminForDid(circleVo.getCreatorDid()),
                circleId, circleVo.getName(), circleVo.getCreatorDid(), circleVo.getCreatorOrgName(), circleVo.getDescription());
        if (isCircle) {
            AgrCircle circle = new AgrCircle();
            circle.setName(circleVo.getName());
            circle.setType((byte) 0);
            circle.setDescription(circleVo.getDescription());
            circle.setCreator(circleVo.getCreatorDid());
            circle.setCreatorOrgName(circleVo.getCreatorOrgName());
            circle.setOwner(circleVo.getCreatorDid());
            circle.setDefaultImage(circleVo.getDefaultImage());
            circle.setStatus((byte) 0);
            circle.setParentId(0);
            circle.setUpdatedAt(LocalDateTime.now());
            circle.setCreatedAt(LocalDateTime.now());
            circle.setCircleId(circleId);
            circleService.save(circle);
            return handleOK();
        }
        return handleFail("创建失败");
    }
    /**
     * 申请加入圈子
     */
    @PostMapping("/circle/join")
    public RespResult joinCircle(@Validated @RequestBody ApplyForVo applyForVo)
    {
        Boolean joinCircleResult = tradeAgreementContractService.applyToJoinCircle(getContractAdminForDid(applyForVo.getMemberDID()), applyForVo.getCircleId(),
                applyForVo.getMemberDID(), applyForVo.getOrgName(), applyForVo.getSocialCreditCode());
        if (joinCircleResult)
        {
            // 当前默认同意
            List<AgrCircle> agrCircleList = circleService.lambdaQuery().eq(AgrCircle::getCircleId, applyForVo.getCircleId()).list();
            if (agrCircleList == null || agrCircleList.isEmpty()) {
                return handleResult("圈子标识: " + applyForVo.getCircleId() + " 未找到关联数据");
            }
            AgrCircle agrCircle = agrCircleList.get(0);
            Boolean approvedResult = tradeAgreementContractService.approveApplication(getContractAdminForDid(applyForVo.getOwnerDID()),
                    applyForVo.getCircleId(), agrCircle.getOwner(), applyForVo.getMemberDID());
            if (approvedResult)
            {
                // 审批后逻辑
//                List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, applyForVo.getCircleId())
//                        .eq(AgrCircleMembers::getMemberDid, applyForVo.getMemberDID()).list();
//                if (agrCircleMembersList != null && !agrCircleMembersList.isEmpty())
//                {
//                    AgrCircleMembers agrCircleMembers = agrCircleMembersList.get(0);
//                    agrCircleMembers.setStatus((byte) 1);
//                    agrCircleMembersService.updateById(agrCircleMembers);
//                }
                // 加入后逻辑
                AgrCircleMembers circleMembers = new AgrCircleMembers();
                circleMembers.setCircleId(applyForVo.getCircleId());
                circleMembers.setMemberDid(applyForVo.getMemberDID());
                circleMembers.setRole((byte) 2);
                circleMembers.setStatus((byte) 1);
                circleMembers.setOrgName(applyForVo.getOrgName());
                circleMembers.setUscc(applyForVo.getSocialCreditCode());
                agrCircleMembersService.save(circleMembers);

                return handleOK();
            }
        }
        return handleFail("加入失败");
    }

    /**
     * 获取圈子申请列表
     * @param circleId 圈子ID
     * @param page 页码
     * @param pageSize 页大小
     */
    @GetMapping("/circle/applications")
    public RespResult getApplications(String circleId, Long page, Long pageSize){
        JSONArray applications = tradeAgreementContractService.getApplications(getContractAdminForDid(null), circleId, page, pageSize);
        return handleResult(applications);
    }

    /**
     * 同意加入圈子
     */
    @PostMapping("/circle/approve")
    public RespResult approveJoinCircle(@Validated @RequestBody ApplyForVo applyForVo){
        Boolean approvedResult = tradeAgreementContractService.approveApplication(getContractAdminForDid(applyForVo.getOwnerDID()),
                applyForVo.getCircleId(), applyForVo.getOwnerDID(), applyForVo.getMemberDID());
        if (approvedResult)
        {
            // 审批后逻辑
            List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, applyForVo.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, applyForVo.getMemberDID()).list();
            if (agrCircleMembersList != null && !agrCircleMembersList.isEmpty())
            {
                AgrCircleMembers agrCircleMembers = agrCircleMembersList.get(0);
                agrCircleMembers.setStatus((byte) 1);
                agrCircleMembersService.updateById(agrCircleMembers);
            }
            return handleOK();
        }
        return handleFail("审批失败");
    }

    /**
     * 拒绝加入圈子
     */
    @PostMapping("/circle/reject")
    public RespResult rejectJoinCircle(@Validated @RequestBody ApplyForVo applyForVo){
        Boolean rejectedResult = tradeAgreementContractService.rejectApplication(getContractAdminForDid(applyForVo.getOwnerDID()),
                applyForVo.getCircleId(), applyForVo.getOwnerDID(), applyForVo.getMemberDID());
        if (rejectedResult)
        {
            // 审批后逻辑
            List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, applyForVo.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, applyForVo.getMemberDID()).list();
            if (agrCircleMembersList != null && !agrCircleMembersList.isEmpty())
            {
                AgrCircleMembers agrCircleMembers = agrCircleMembersList.get(0);
                agrCircleMembers.setStatus((byte) 0);
                agrCircleMembersService.updateById(agrCircleMembers);
            }
            return handleOK();
        }
        return handleFail("审批失败");
    }

    /**
     * 退出圈子
     */
    @PostMapping("/circle/quit")
    public RespResult quitCircle(@Validated @RequestBody ApplyForVo applyForVo)
    {
        Boolean exitCircleResult = tradeAgreementContractService.exitCircle(getContractAdminForDid(applyForVo.getMemberDID()),
                applyForVo.getCircleId(), applyForVo.getMemberDID());
        if (exitCircleResult)
        {
            // 退出后逻辑
            agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, applyForVo.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, applyForVo.getMemberDID()).list().forEach(agrCircleMembersService::removeById);

            return handleOK();
        }
        return handleFail("退出失败");
    }

    /**
     * 圈子详情
     */
    @GetMapping("/circle/detail")
    public RespResult circleDetail(String circleId,String memberDid)
    {
        // 圈子详情
        List<AgrCircle> agrCircleList = circleService.lambdaQuery().eq(AgrCircle::getCircleId, circleId).list();
        if (agrCircleList != null && !agrCircleList.isEmpty())
        {
            AgrCircle agrCircle = agrCircleList.get(0);
            CircleVo circleVo = new CircleVo();
            circleVo.setId(agrCircle.getId());
            circleVo.setCircleId(agrCircle.getCircleId());
            circleVo.setName(agrCircle.getName());
            circleVo.setDescription(agrCircle.getDescription());
            circleVo.setCreatorDid(agrCircle.getCreator());
            circleVo.setCreatorOrgName(agrCircle.getCreatorOrgName());
            circleVo.setMemberCount(Math.toIntExact(agrCircleMembersService.lambdaQuery()
                    .eq(AgrCircleMembers::getCircleId, agrCircle.getCircleId())
                            .eq(AgrCircleMembers::getStatus, 1).count()));
            circleVo.setOwnerDid(agrCircle.getOwner());
            circleVo.setOwnerOrgName(agrCircle.getOwnerOrgName());
            circleVo.setIsJoin(agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getCircleId, agrCircle.getCircleId())
                    .eq(AgrCircleMembers::getMemberDid, memberDid).exists());
            return handleResult(circleVo);
        }
        return handleFail("未查询到该圈子详情");
    }

    /**
     * 圈子成员分页列表
     * @param circleId 圈子标识
     * @param pageNo 页码
     * @param pageSize 每页数量
     */
    @GetMapping("/circle/member/list")
    public RespResult circleMemberList(String circleId, Long pageNo, Long pageSize){
        com.aeotrade.server.chain.config.Page<CircleMemberVO> agrCircleMembersVOPage=new com.aeotrade.server.chain.config.Page<>();
        if(pageNo <=0){
            pageNo = 1L;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10L;
        }
        Page<AgrCircleMembers> pageResult = agrCircleMembersService.page(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<AgrCircleMembers>().eq(AgrCircleMembers::getCircleId, circleId)
                        .eq(AgrCircleMembers::getStatus, 1).ne(AgrCircleMembers::getRole, (byte)0));
        agrCircleMembersVOPage.setTotal(pageResult.getTotal());
        agrCircleMembersVOPage.setRecords(pageResult.getRecords().stream().map(agrCircleMembers -> {
            CircleMemberVO circleMemberVO = new CircleMemberVO();
            circleMemberVO.setId(agrCircleMembers.getId());
            circleMemberVO.setMemberDid(agrCircleMembers.getMemberDid());
            circleMemberVO.setOrgName(agrCircleMembers.getOrgName());
            return circleMemberVO;
        }).collect(Collectors.toList()));
        return handleResult(agrCircleMembersVOPage);
    }

    /**
     * 我创建的圈子列表
     */
    @GetMapping("/circle/my")
    public RespResult myCircleList(String did)
    {
        return handleResult(circleService.lambdaQuery().eq(AgrCircle::getCreator, did).list());
    }
    /**
     * 我加入的圈子列表
     */
    @GetMapping("/circle/my/join")
    public RespResult myJoinCircleList(String did)
    {
        List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery().eq(AgrCircleMembers::getMemberDid, did).eq(AgrCircleMembers::getStatus, 1).list();
        if (agrCircleMembersList.isEmpty()){
            return handleResult(new ArrayList<>());
        }
        return handleResult(circleService.lambdaQuery().ne(AgrCircle::getCreator, did).in(AgrCircle::getCircleId, agrCircleMembersList.stream().map(AgrCircleMembers::getCircleId).collect(Collectors.toList())).list());
    }

    /**
     * 我拥有的所有圈子
     * @param did
     */
    @GetMapping("/circle/my/own")
    public RespResult myOwnCircleList(String did){
        return handleResult(circleService.myOwnCircleList( did));
    }
    /**
     * 推荐圈子列表
     */
    @GetMapping("/circle/recommend")
    public RespResult recommendCircleList()
    {
        if (recommendCircleIds.isEmpty()){
            return handleResult(new ArrayList<>());
        }
        return handleResult(circleService.lambdaQuery().in(AgrCircle::getCircleId, recommendCircleIds).list());
    }
}

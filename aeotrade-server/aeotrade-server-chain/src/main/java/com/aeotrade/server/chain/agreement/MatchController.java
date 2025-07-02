package com.aeotrade.server.chain.agreement;

import com.aeotrade.server.tradeagreement.entity.AgrCircle;
import com.aeotrade.server.tradeagreement.entity.AgrCircleMembers;
import com.aeotrade.server.tradeagreement.entity.AgrMatch;
import com.aeotrade.server.tradeagreement.entity.AgrMatchInfo;
import com.aeotrade.server.tradeagreement.service.IAgrCircleMembersService;
import com.aeotrade.server.tradeagreement.service.IAgrCircleService;
import com.aeotrade.server.tradeagreement.service.IAgrMatchInfoService;
import com.aeotrade.server.tradeagreement.service.IAgrMatchService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 匹配信息
 */
@RestController
public class MatchController extends BaseController {

    private final IAgrMatchInfoService agrMatchInfoService;
    private final IAgrCircleService agrCircleService;
    private final IAgrCircleMembersService agrCircleMembersService;
    private final IAgrMatchService agrMatchService;

    public MatchController(IAgrMatchInfoService agrMatchInfoService, IAgrCircleService agrCircleService, IAgrCircleMembersService agrCircleMembersService, IAgrMatchService agrMatchService) {
        this.agrMatchInfoService = agrMatchInfoService;
        this.agrCircleService = agrCircleService;
        this.agrCircleMembersService = agrCircleMembersService;
        this.agrMatchService = agrMatchService;
    }

    /**
     * 供需分析匹配
     */
    @GetMapping("/info/match")
    public RespResult match(String memberDID) {
        //查询当前用户所有信息
        List<AgrMatchInfo> list = agrMatchInfoService.lambdaQuery().eq(AgrMatchInfo::getMemberDid, memberDID)
                .eq(AgrMatchInfo::getStatus, 3).list();
        //获取需求数量
        List<String> requirementList = list.stream().filter(item -> item.getType() == 0).map(AgrMatchInfo::getTxId).collect(Collectors.toList());
        long requirementCount = requirementList.size();
        //获取供应产品数量
        List<String> productList = list.stream().filter(item -> item.getType() == 1).map(AgrMatchInfo::getTxId).collect(Collectors.toList());
        long productCount = productList.size();
        //设置 匹配内容状态
        StringBuffer matchStatus = new StringBuffer();
        if (requirementCount > 0) {
            matchStatus.append("a");
        }
        if (productCount > 0) {
            matchStatus.append("b");
        }
        //获取当前用户的所有圈子
        Set<AgrCircle> ownCircleList = agrCircleService.myOwnCircleList(memberDID);
        List<String> circleIdList = ownCircleList.stream().map(AgrCircle::getCircleId).collect(Collectors.toList());
        Long circleMemberCount = 0L;
        Long productAllCount = 0L;
        Long requirementAllCount = 0L;
        if (!circleIdList.isEmpty()) {
            //根据圈子获取所有企业数量
            List<AgrCircleMembers> agrCircleMembersList = agrCircleMembersService.lambdaQuery()
                    .in(AgrCircleMembers::getCircleId, circleIdList).list();
            circleMemberCount = agrCircleMembersList.stream().map(AgrCircleMembers::getMemberDid).distinct().count();
            //查询当前用户所有圈子的需求总数
            requirementAllCount = agrMatchInfoService.lambdaQuery().in(AgrMatchInfo::getCircleId, circleIdList).eq(AgrMatchInfo::getType, 0)
                    .eq(AgrMatchInfo::getStatus, 3).count();
            //查询当前用户所有圈子的供应总数
            productAllCount = agrMatchInfoService.lambdaQuery().in(AgrMatchInfo::getCircleId, circleIdList).eq(AgrMatchInfo::getType, 1)
                    .eq(AgrMatchInfo::getStatus, 3).count();
        }
        //需求匹配到产品的数量
        Long demandMatchSupplyCount = 0L;
        if (!requirementList.isEmpty()) {
            //查询当前用户所有圈子的需求总数
            demandMatchSupplyCount = agrMatchService.lambdaQuery().in(AgrMatch::getDemandTxId, requirementList).count();
         }
        //供应匹配到需求的数量
        Long supplyMatchDemandCount = 0L;
        if (!productList.isEmpty()) {
            supplyMatchDemandCount = agrMatchService.lambdaQuery().in(AgrMatch::getSupplyTxId, productList).count();
        }

        //返回结果，包括两个对象，一个匹配需求，一个匹配供应
        JSONObject result = new JSONObject();
        JSONObject requirement = new JSONObject();
        requirement.put("count", requirementCount);//根据当前用户，查询需求数量
        requirement.put("circle", String.valueOf(ownCircleList.size()));// 根据当前用户，查询圈子数量
        requirement.put("supplier", circleMemberCount);// 根据圈子表，查询供应商数量，注：供需商数据一致
        requirement.put("product", productAllCount);// 根据圈子列表，查询产品数量
        requirement.put("match", demandMatchSupplyCount); //需求匹配到产品的数量

        JSONObject product = new JSONObject();
        product.put("count", productCount);//根据当前用户，查询产品数量
        product.put("circle", String.valueOf(ownCircleList.size()));// 根据当前用户，查询圈子数量
        product.put("supplier", circleMemberCount);// 根据圈子表，查询供应商数量，注：供需商数据一致
        product.put("requirement", requirementAllCount);// 根据圈子列表，查询需求数量
        product.put("match", supplyMatchDemandCount); //产品匹配到需求数量

        result.put("requirement", requirement);
        result.put("product", product);
        //a:只有需求；b:只有产品；ab:都有; 空字符:都没有
        result.put("status", matchStatus);
        return handleResult(result);
    }

    /**
     * 需求匹配产品列表
     * @param memberDID 企业组织DID标识
     * @param demandName 搜索需求名称
     * @param supplyName 搜索产品名称
     * @param pageNo 页码
     * @param pageSize 每页数量
     */
    @GetMapping("/match/supply/list")
    public RespResult matchSupplyList(String memberDID,@RequestParam(required = false) String demandName, String supplyName,
                                      @RequestParam(defaultValue = "1") Long pageNo, @RequestParam(defaultValue = "10") Long pageSize) {
        com.aeotrade.server.chain.config.Page<AgrMatch> agrMatchPage=new com.aeotrade.server.chain.config.Page<>();
        if(pageNo <=0){
            pageNo = 1L;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10L;
        }

        Page<AgrMatch> pageResult = agrMatchService.page(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<AgrMatch>().eq(AgrMatch::getDemandMemberDid,memberDID)
                        .like(!StringUtils.isEmpty(demandName), AgrMatch::getDemandName, demandName)
                        .like(!StringUtils.isEmpty(supplyName), AgrMatch::getSupplyName, supplyName)
                        .orderByDesc(AgrMatch::getCreatedAt)
        );
        agrMatchPage.setTotal(pageResult.getTotal());
        agrMatchPage.setRecords(pageResult.getRecords().stream().map(agrMatch -> {
            agrMatch.setDetailId(agrMatch.getSupplyId());
            return agrMatch;
        }).collect(Collectors.toList()));
        return handleResult(agrMatchPage);
    }

    /**
     * 产品匹配需求列表
     * @param memberDID 企业组织DID标识
     * @param demandName 搜索需求名称
     * @param supplyName 搜索产品名称
     * @param pageNo 页码
     * @param pageSize 每页数量
     */
    @GetMapping("/match/demand/list")
    public RespResult matchDemandList(String memberDID,@RequestParam(required = false) String demandName, String supplyName,
                                      @RequestParam(defaultValue = "1") Long pageNo, @RequestParam(defaultValue = "10") Long pageSize) {
        com.aeotrade.server.chain.config.Page<AgrMatch> agrMatchPage=new com.aeotrade.server.chain.config.Page<>();
        if(pageNo <=0){
            pageNo = 1L;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10L;
        }

        Page<AgrMatch> pageResult = agrMatchService.page(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<AgrMatch>().eq(AgrMatch::getDemandMemberDid,memberDID)
                        .like(!StringUtils.isEmpty(demandName), AgrMatch::getDemandName, demandName)
                        .like(!StringUtils.isEmpty(supplyName), AgrMatch::getSupplyName, supplyName)
                        .orderByDesc(AgrMatch::getCreatedAt)
        );
        agrMatchPage.setTotal(pageResult.getTotal());
        agrMatchPage.setRecords(pageResult.getRecords().stream().map(agrMatch -> {
            agrMatch.setDetailId(agrMatch.getDemandId());
            return agrMatch;
        }).collect(Collectors.toList()));
        return handleResult(agrMatchPage);
    }
}

package com.aeotrade.server.chain.controller;

import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.server.chain.service.ChainCertUserMemberService;

import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 用户证书管理 前端控制器
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@RestController
@RequestMapping("/chain/cert/user/member")
@Slf4j
public class ChainCertUserMemberController extends BaseController{
    @Autowired
    private ChainCertUserMemberService chainCertUserMemberService;
    @Value("${hmtx.browser.server-url}")
    private String HMTX_BROWSER_SERVER;
  /*

    
    *//**
    * 通过id查询
    *//*
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id){
        return handleResult(chainCertUserMemberService.getById(id));
    }

    *//**
    * 新增
    *//*
    @PostMapping("/save")
    public RespResult save(@RequestBody ChainCertUserMember chainCertUserMember){
        chainCertUserMemberService.save(chainCertUserMember);
        return handleOK();
    }

    *//**
    * 通过id删除
    *//*
    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            chainCertUserMemberService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    *//**
    * 修改
    *//*
    @PutMapping("/update")
    public RespResult updateById(@RequestBody ChainCertUserMember chainCertUserMember){
        chainCertUserMemberService.updateById(chainCertUserMember);
        return handleOK();
    }


    *//**
    * 查询列表
    *//*
    @PostMapping("/list")
    public RespResult<List> list(@RequestBody ChainCertUserMemberReqVo chainCertUserMember ){
    final LambdaQueryWrapper<ChainCertUserMember> lambda = new QueryWrapper<ChainCertUserMember>().lambda();
        this.buildCondition(lambda,chainCertUserMember);
        return handleResult(chainCertUserMemberService.list(lambda));
    }
*/

    /**
     * 查询列表
     */
    @GetMapping("/list/staff")
    public RespResult listStaff(@RequestParam Integer pageSize,@RequestParam Integer pageNo,@RequestParam String staffId) throws Exception {
        return handleResult(chainCertUserMemberService.listStaff(pageNo,pageSize,staffId));
    }

    /**
     * 删除角色关联
     */
    @GetMapping("/delete/staff")
    public RespResult deleteStaff(@RequestParam String memberId,@RequestParam String staffId) throws Exception {
        return handleResult(chainCertUserMemberService.deleteStaff(memberId,staffId));
    }

    /**
     * 获取用户证书
     */
    @GetMapping("/find/user")
    public RespResult findUser(@RequestParam String memberId,@RequestParam String userId) throws Exception {
        return handleResult(chainCertUserMemberService.findUser(memberId,userId));
    }

    /**
     * 查询交易记录
     */
    @GetMapping("/find/deal")
    public RespResult findDeal(String ChainId,String Limit,String TxId,String Offset,String StartTime,String EndTime
            ,String cmb,String OrgId,String BlockHeight,String BlockHash,String Sender,String ContractName) throws Exception {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("ChainId",ChainId);
            map.put("Limit", Limit);
            map.put("TxId", TxId);
            map.put("Offset", Offset);
            map.put("StartTime", StartTime);
            map.put("EndTime", EndTime);
            map.put("cmb", cmb);
            map.put("OrgId", OrgId);
            map.put("BlockHeight", BlockHeight);
            map.put("BlockHash", BlockHash);
            map.put("Sender", Sender);
            map.put("ContractName",ContractName);
            String http = HttpRequestUtils.httpGet(HMTX_BROWSER_SERVER, map);
            return handleResult(http);
        }catch (Exception e){
            log.info("区块链请求超时");
            return handleOK();
        }
    }

    /**
     * 查询交易详情
     */
    @GetMapping("/find/deal/info")
    public RespResult findDeal(String ChainId,String TxId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("ChainId",ChainId);
        map.put("TxId", TxId);
        map.put("cmb","GetTxDetail");
        String http = HttpRequestUtils.httpGet(HMTX_BROWSER_SERVER, map);
        return handleResult(http);
    }
  /*  *//**
    * 分页查询
    *//*
    @PostMapping("/page")
    public RespResult<PageResult<ChainCertUserMember>> page(@RequestBody PageParam<ChainCertUserMemberReqVo> pageParam){
        final ChainCertUserMemberReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<ChainCertUserMember> lambda = new QueryWrapper<ChainCertUserMember>().lambda();
        this.buildCondition(lambda,param);
        final IPage<ChainCertUserMember> page = chainCertUserMemberService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<ChainCertUserMember> pr = new PageResult();
        pr.setPageCount(page.getPages());
        pr.setTotalCount(page.getTotal());
        pr.setPageNo(new Long(page.getCurrent()).intValue());
        pr.setPageSize((int) page.getSize());
        pr.setResults(page.getRecords());
        return handleResult(pr);
    }


        *//**
        * 构造查询条件
        * @param lambda
        * @param param
        *//*
        private void buildCondition(LambdaQueryWrapper<ChainCertUserMember> lambda, ChainCertUserMemberReqVo param){
            if(!StringUtils.isEmpty(param.getId())){
                lambda.eq(ChainCertUserMember::getId, param.getId());
            }
            if(!CollectionUtils.isEmpty(param.getCreateAtList())){
                lambda.ge(ChainCertUserMember::getCreateAt, param.getCreateAtList().get(0));
                lambda.le(ChainCertUserMember::getCreateAt, param.getCreateAtList().get(1));
            }
            if(!CollectionUtils.isEmpty(param.getUpdateAtList())){
                lambda.ge(ChainCertUserMember::getUpdateAt, param.getUpdateAtList().get(0));
                lambda.le(ChainCertUserMember::getUpdateAt, param.getUpdateAtList().get(1));
            }
            if(!StringUtils.isEmpty(param.getTenantId())){
                lambda.eq(ChainCertUserMember::getTenantId, param.getTenantId());
            }
            if(!StringUtils.isEmpty(param.getUserId())){
                lambda.eq(ChainCertUserMember::getUserId, param.getUserId());
            }
            if(!StringUtils.isEmpty(param.getCertType())){
                lambda.eq(ChainCertUserMember::getCertType, param.getCertType());
            }
            if(!StringUtils.isEmpty(param.getSignCertId())){
                lambda.eq(ChainCertUserMember::getSignCertId, param.getSignCertId());
            }
            if(!StringUtils.isEmpty(param.getTlsCertId())){
                lambda.eq(ChainCertUserMember::getTlsCertId, param.getTlsCertId());
            }
            if(!StringUtils.isEmpty(param.getIsDel())){
                lambda.eq(ChainCertUserMember::getIsDel, param.getIsDel());
            }
            lambda.orderBy(true,false, ChainCertUserMember::getId);
        }


        *//**
         * 请求model
         *//*
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class ChainCertUserMemberReqVo extends ChainCertUserMember {
            private List<String> createAtList; // 起止
            private List<String> updateAtList; // 起止
        }*/


}

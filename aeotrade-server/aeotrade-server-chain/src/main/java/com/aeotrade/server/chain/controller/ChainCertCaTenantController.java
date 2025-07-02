package com.aeotrade.server.chain.controller;

import com.aeotrade.server.chain.service.IChainCertCaTenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;


/**
 * <p>
 * 企业租户证书管理 前端控制器
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@RestController
@RequestMapping("/chain/cert/ca/tenant")
public class ChainCertCaTenantController extends BaseController{
    @Autowired
    private IChainCertCaTenantService iChainCertCaTenantService;
   /*
    *//**
    * 新增
    *//*
    @PostMapping("/save")
    public RespResult save(@RequestBody ChainCertCaTenant chainCertCaTenant){
        iChainCertCaTenantService.save(chainCertCaTenant);
        return handleOK();
    }

    *//**
    * 通过id删除
    *//*
    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            iChainCertCaTenantService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    *//**
    * 修改
    *//*
    @PutMapping("/update")
    public RespResult updateById(@RequestBody ChainCertCaTenant chainCertCaTenant){
        iChainCertCaTenantService.updateById(chainCertCaTenant);
        return handleOK();
    }


    *//**
    * 查询列表
    *//*
    @PostMapping("/list")
    public RespResult<List> list(@RequestBody ChainCertCaTenantReqVo chainCertCaTenant ){
    final LambdaQueryWrapper<ChainCertCaTenant> lambda = new QueryWrapper<ChainCertCaTenant>().lambda();
        this.buildCondition(lambda,chainCertCaTenant);
        return handleResult(iChainCertCaTenantService.list(lambda));
    }*/

    /**
     * 根据企业id查询节点列表
     */
    @GetMapping("/findById")
    public RespResult findById(@RequestParam String memberId,@RequestParam String staffId,@RequestParam Integer pageNo,@RequestParam Integer pageSize,@RequestParam Integer isAdmin){
        return handleResult(iChainCertCaTenantService.findById(memberId,staffId,pageNo,pageSize,isAdmin));
    }

    /**
     * 根据企业id查询节点列表
     */
    @GetMapping("/find/ByRobotId")
    public RespResult findByRobotId(@RequestParam String memberId){
        return handleResult(iChainCertCaTenantService.findByRobotId(memberId));
    }
    /**
     * 通过id查询
     */
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") String id){
        return handleResult(iChainCertCaTenantService.getById(id));
    }

    /**
     * 通过id查询
     */
    @GetMapping("/get/byuscc")
    public RespResult getByUscc(@RequestParam String org_usc_code) throws Exception {
        return handleResult(iChainCertCaTenantService.getByUscc(org_usc_code));
    }


   /* *//**
    * 分页查询
    *//*
    @PostMapping("/page")
    public RespResult<PageResult<ChainCertCaTenant>> page(@RequestBody PageParam<ChainCertCaTenantReqVo> pageParam){
        final ChainCertCaTenantReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<ChainCertCaTenant> lambda = new QueryWrapper<ChainCertCaTenant>().lambda();
        this.buildCondition(lambda,param);
        final IPage<ChainCertCaTenant> page = iChainCertCaTenantService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<ChainCertCaTenant> pr = new PageResult();
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
        private void buildCondition(LambdaQueryWrapper<ChainCertCaTenant> lambda, ChainCertCaTenantReqVo param){
            if(!StringUtils.isEmpty(param.getId())){
                lambda.eq(ChainCertCaTenant::getId, param.getId());
            }
            if(!CollectionUtils.isEmpty(param.getCreateAtList())){
                lambda.ge(ChainCertCaTenant::getCreateAt, param.getCreateAtList().get(0));
                lambda.le(ChainCertCaTenant::getCreateAt, param.getCreateAtList().get(1));
            }
            if(!CollectionUtils.isEmpty(param.getUpdateAtList())){
                lambda.ge(ChainCertCaTenant::getUpdateAt, param.getUpdateAtList().get(0));
                lambda.le(ChainCertCaTenant::getUpdateAt, param.getUpdateAtList().get(1));
            }
            if(!StringUtils.isEmpty(param.getTenantId())){
                lambda.eq(ChainCertCaTenant::getTenantId, param.getTenantId());
            }
            if(!StringUtils.isEmpty(param.getChainId())){
                lambda.eq(ChainCertCaTenant::getChainId, param.getChainId());
            }
            if(!StringUtils.isEmpty(param.getCertId())){
                lambda.eq(ChainCertCaTenant::getCertId, param.getCertId());
            }
            if(!StringUtils.isEmpty(param.getIsVote())){
                lambda.eq(ChainCertCaTenant::getIsVote, param.getIsVote());
            }
            if(!StringUtils.isEmpty(param.getIsDel())){
                lambda.eq(ChainCertCaTenant::getIsDel, param.getIsDel());
            }
            lambda.orderBy(true,false, ChainCertCaTenant::getId);
        }


        *//**
         * 请求model
         *//*
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class ChainCertCaTenantReqVo extends ChainCertCaTenant {
            private List<String> createAtList; // 起止
            private List<String> updateAtList; // 起止
        }
*/

}

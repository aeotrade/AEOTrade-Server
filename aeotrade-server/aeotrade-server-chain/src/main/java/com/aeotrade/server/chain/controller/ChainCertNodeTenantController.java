package com.aeotrade.server.chain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.PageParam;
import com.aeotrade.suppot.PageResult;
import org.springframework.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.aeotrade.server.chain.service.ChainCertNodeTenantService;
import java.util.*;
import lombok.Data;


/**
 * <p>
 * 租户上链节点管理 前端控制器
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@RestController
@RequestMapping("/chain/cert/node/tenant")
public class ChainCertNodeTenantController extends BaseController{
/*

    
    @Autowired
    private ChainCertNodeTenantService chainCertNodeTenantService;

    
    */
/**
    * 通过id查询
    *//*

    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id){
        return handleResult(chainCertNodeTenantService.getById(id));
    }

    */
/**
    * 新增
    *//*

    @PostMapping("/save")
    public RespResult save(@RequestBody ChainCertNodeTenant chainCertNodeTenant){
        chainCertNodeTenantService.save(chainCertNodeTenant);
        return handleOK();
    }

    */
/**
    * 通过id删除
    *//*

    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            chainCertNodeTenantService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    */
/**
    * 修改
    *//*

    @PutMapping("/update")
    public RespResult updateById(@RequestBody ChainCertNodeTenant chainCertNodeTenant){
        chainCertNodeTenantService.updateById(chainCertNodeTenant);
        return handleOK();
    }


    */
/**
    * 查询列表
    *//*

    @PostMapping("/list")
    public RespResult<List> list(@RequestBody ChainCertNodeTenantReqVo chainCertNodeTenant ){
    final LambdaQueryWrapper<ChainCertNodeTenant> lambda = new QueryWrapper<ChainCertNodeTenant>().lambda();
        this.buildCondition(lambda,chainCertNodeTenant);
        return handleResult(chainCertNodeTenantService.list(lambda));
    }

    */
/**
    * 分页查询
    *//*

    @PostMapping("/page")
    public RespResult<PageResult<ChainCertNodeTenant>> page(@RequestBody PageParam<ChainCertNodeTenantReqVo> pageParam){
        final ChainCertNodeTenantReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<ChainCertNodeTenant> lambda = new QueryWrapper<ChainCertNodeTenant>().lambda();
        this.buildCondition(lambda,param);
        final IPage<ChainCertNodeTenant> page = chainCertNodeTenantService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<ChainCertNodeTenant> pr = new PageResult();
        pr.setPageCount(page.getPages());
        pr.setTotalCount(page.getTotal());
        pr.setPageNo(new Long(page.getCurrent()).intValue());
        pr.setPageSize((int) page.getSize());
        pr.setResults(page.getRecords());
        return handleResult(pr);
    }


        */
/**
        * 构造查询条件
        * @param lambda
        * @param param
        *//*

        private void buildCondition(LambdaQueryWrapper<ChainCertNodeTenant> lambda, ChainCertNodeTenantReqVo param){
            if(!StringUtils.isEmpty(param.getId())){
                lambda.eq(ChainCertNodeTenant::getId, param.getId());
            }
            if(!CollectionUtils.isEmpty(param.getCreateAtList())){
                lambda.ge(ChainCertNodeTenant::getCreateAt, param.getCreateAtList().get(0));
                lambda.le(ChainCertNodeTenant::getCreateAt, param.getCreateAtList().get(1));
            }
            if(!CollectionUtils.isEmpty(param.getUpdateAtList())){
                lambda.ge(ChainCertNodeTenant::getUpdateAt, param.getUpdateAtList().get(0));
                lambda.le(ChainCertNodeTenant::getUpdateAt, param.getUpdateAtList().get(1));
            }
            if(!StringUtils.isEmpty(param.getTenantId())){
                lambda.eq(ChainCertNodeTenant::getTenantId, param.getTenantId());
            }
            if(!StringUtils.isEmpty(param.getChainId())){
                lambda.eq(ChainCertNodeTenant::getChainId, param.getChainId());
            }
            if(!StringUtils.isEmpty(param.getCertId())){
                lambda.eq(ChainCertNodeTenant::getCertId, param.getCertId());
            }
            if(!StringUtils.isEmpty(param.getNodeAddr())){
                lambda.eq(ChainCertNodeTenant::getNodeAddr, param.getNodeAddr());
            }
            if(!StringUtils.isEmpty(param.getTlsHostName())){
                lambda.eq(ChainCertNodeTenant::getTlsHostName, param.getTlsHostName());
            }
            if(!StringUtils.isEmpty(param.getIsDel())){
                lambda.eq(ChainCertNodeTenant::getIsDel, param.getIsDel());
            }
            lambda.orderBy(true,false, ChainCertNodeTenant::getId);
        }


        */
/**
         * 请求model
         *//*

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class ChainCertNodeTenantReqVo extends ChainCertNodeTenant {
            private List<String> createAtList; // 起止
            private List<String> updateAtList; // 起止
        }
*/


}

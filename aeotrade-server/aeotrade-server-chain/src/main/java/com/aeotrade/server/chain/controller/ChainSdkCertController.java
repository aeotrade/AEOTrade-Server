package com.aeotrade.server.chain.controller;

import org.springframework.web.bind.annotation.*;
import com.aeotrade.suppot.BaseController;


/**
 * <p>
 * 上链发送交易时所有需要用到的证书数据 前端控制器
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@RestController
@RequestMapping("/chain/sdk/cert")
public class ChainSdkCertController extends BaseController{

    
  /*  @Autowired
    private ChainSdkCertService chainSdkCertService;

    
    *//**
    * 通过id查询
    *//*
    @GetMapping("/get/byid/{id}")
    public RespResult getById(@PathVariable(value = "id") Long id){
        return handleResult(chainSdkCertService.getById(id));
    }

    *//**
    * 新增
    *//*
    @PostMapping("/save")
    public RespResult save(@RequestBody ChainSdkCert chainSdkCert){
        chainSdkCertService.save(chainSdkCert);
        return handleOK();
    }

    *//**
    * 通过id删除
    *//*
    @DeleteMapping("/delete/{id}")
    public RespResult delete(@PathVariable(value = "id") String ids){
        String[] idsStrs = ids.split(",");
        for (String id:idsStrs){
            chainSdkCertService.removeById(Long.valueOf(id));
        }
        return handleOK();
    }

    *//**
    * 修改
    *//*
    @PutMapping("/update")
    public RespResult updateById(@RequestBody ChainSdkCert chainSdkCert){
        chainSdkCertService.updateById(chainSdkCert);
        return handleOK();
    }


    *//**
    * 查询列表
    *//*
    @PostMapping("/list")
    public RespResult<List> list(@RequestBody ChainSdkCertReqVo chainSdkCert ){
    final LambdaQueryWrapper<ChainSdkCert> lambda = new QueryWrapper<ChainSdkCert>().lambda();
        this.buildCondition(lambda,chainSdkCert);
        return handleResult(chainSdkCertService.list(lambda));
    }

    *//**
    * 分页查询
    *//*
    @PostMapping("/page")
    public RespResult<PageResult<ChainSdkCert>> page(@RequestBody PageParam<ChainSdkCertReqVo> pageParam){
        final ChainSdkCertReqVo param = pageParam.getParam();
        final LambdaQueryWrapper<ChainSdkCert> lambda = new QueryWrapper<ChainSdkCert>().lambda();
        this.buildCondition(lambda,param);
        final IPage<ChainSdkCert> page = chainSdkCertService.page(new Page<>(pageParam.getPageNo(), pageParam.getPageSize()), lambda);
        PageResult<ChainSdkCert> pr = new PageResult();
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
        private void buildCondition(LambdaQueryWrapper<ChainSdkCert> lambda, ChainSdkCertReqVo param){
            if(!StringUtils.isEmpty(param.getId())){
                lambda.eq(ChainSdkCert::getId, param.getId());
            }
            if(!CollectionUtils.isEmpty(param.getCreateAtList())){
                lambda.ge(ChainSdkCert::getCreateAt, param.getCreateAtList().get(0));
                lambda.le(ChainSdkCert::getCreateAt, param.getCreateAtList().get(1));
            }
            if(!CollectionUtils.isEmpty(param.getUpdateAtList())){
                lambda.ge(ChainSdkCert::getUpdateAt, param.getUpdateAtList().get(0));
                lambda.le(ChainSdkCert::getUpdateAt, param.getUpdateAtList().get(1));
            }
            if(!StringUtils.isEmpty(param.getCertType())){
                lambda.eq(ChainSdkCert::getCertType, param.getCertType());
            }
            if(!StringUtils.isEmpty(param.getCertUse())){
                lambda.eq(ChainSdkCert::getCertUse, param.getCertUse());
            }
            if(!StringUtils.isEmpty(param.getCert())){
                lambda.eq(ChainSdkCert::getCert, param.getCert());
            }
            if(!StringUtils.isEmpty(param.getPrivateKey())){
                lambda.eq(ChainSdkCert::getPrivateKey, param.getPrivateKey());
            }
            if(!StringUtils.isEmpty(param.getOrgId())){
                lambda.eq(ChainSdkCert::getOrgId, param.getOrgId());
            }
            if(!StringUtils.isEmpty(param.getOrgName())){
                lambda.eq(ChainSdkCert::getOrgName, param.getOrgName());
            }
            if(!StringUtils.isEmpty(param.getCertName())){
                lambda.eq(ChainSdkCert::getCertName, param.getCertName());
            }
            if(!StringUtils.isEmpty(param.getExpirationDate())){
                lambda.eq(ChainSdkCert::getExpirationDate, param.getExpirationDate());
            }
            if(!StringUtils.isEmpty(param.getIsDel())){
                lambda.eq(ChainSdkCert::getIsDel, param.getIsDel());
            }
            lambda.orderBy(true,false, ChainSdkCert::getId);
        }


        *//**
         * 请求model
         *//*
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        private static class ChainSdkCertReqVo extends ChainSdkCert {
            private List<String> createAtList; // 起止
            private List<String> updateAtList; // 起止
        }*/


}

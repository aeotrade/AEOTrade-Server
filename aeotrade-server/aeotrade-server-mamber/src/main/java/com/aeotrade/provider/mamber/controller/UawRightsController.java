package com.aeotrade.provider.mamber.controller;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawRights;
import com.aeotrade.provider.mamber.entity.UawRightsRightsType;
import com.aeotrade.provider.mamber.entity.UawRightsType;
import com.aeotrade.provider.mamber.service.impl.UawRightsRightsTypeServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawRightsServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawRightsTypeServiceImpl;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UawRightsController  权益controller
 * @author wuhao
 */
@RestController
@RequestMapping("/uaw/Rights/")
@Slf4j
public class UawRightsController extends BaseController {
    @Autowired
    private UawRightsServiceImpl uawRightsService;
    @Autowired
    private UawRightsTypeServiceImpl uawRightsTypeService;
    @Autowired
    private UawRightsRightsTypeServiceImpl uawRigTypeService;

    /**
     * 运营端列出所有的权益类型列表
     *
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "运营端列出所有的权益列表")
    public RespResult findPageAll(Long id, @RequestParam Integer pageSize, @RequestParam Integer pageNo) {
        try {
            LambdaQueryWrapper<UawRights> uawRightsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            if (id!=null) {
                uawRightsLambdaQueryWrapper.eq(UawRights::getRightsTypeId, id);
            }
            uawRightsLambdaQueryWrapper.eq(UawRights::getStatus, 0);
            //调用分页方法进行权益项查询
            Page<UawRights> page = uawRightsService.page(new Page(pageNo,pageSize), uawRightsLambdaQueryWrapper);
            //循环遍历分页集合
            for (UawRights rights : page.getRecords()) {
                //根据权益类型id得到权益类型对象
                UawRightsType uawRightsType = uawRightsTypeService.getById(rights.getRightsTypeId());
                //将得到的权益类型对象名称取出添加到权益项对象中
                rights.setRightsTypeName(uawRightsType.getRightsTypeName());
                if(StringUtils.isNotEmpty(rights.getResourceName())){
                    String cloud = uawRightsTypeService.findCloudById(rights.getResourceName());
                    if(StringUtils.isNotEmpty(cloud)){
                        rights.setResourceName(cloud);
                    }else{
                        rights.setResourceName("未关联应用或应用已下架");
                    }
                }
            }
            PageList<UawRights> uawRights=new PageList<>();
            uawRights.setTotalSize(page.getTotal());
            uawRights.setRecords(page.getRecords());
            return handleResultList(uawRights);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端添加权益
     *
     * @param uawRights
     * @return
     */
    @PostMapping("insert")
   // @ApiOperation(httpMethod = "POST", value = "运营端添加权益类型")
    public RespResult insertRights(@RequestBody UawRights uawRights) {
        try {
            if (uawRights == null) {
                throw new AeotradeException("权益类型不能为空");
            }
            if (uawRights.getUsePattern() == 0 && StringUtils.isEmpty(uawRights.getResourceName())) {
                throw new AeotradeException("线上服务,应用地址不能为空");
            }
            Boolean insert = uawRightsService.save(uawRights);
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端删除权益
     *
     * @param
     * @return
     */
    @GetMapping("delete")
    //@ApiOperation(httpMethod = "GET", value = "运营端删除权益")
    //@ApiImplicitParam(name = "id", value = "权益id",required=true)
    public RespResult deleteRights(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("权益id不能为空");
            }
            UawRightsRightsType uawRigType = new UawRightsRightsType();
            uawRigType.setRightsId(id);
            List<UawRightsRightsType> list = uawRigTypeService.lambdaQuery().eq(UawRightsRightsType::getRightsId,id)
                    .eq(UawRightsRightsType::getStatus,1).list();
            if(list.size()==0){
                UawRights uawRights = new UawRights();
                uawRights.setId(id);
                uawRights.setStatus(1);
                uawRightsService.updateById(uawRights);
            }else{
                return handleFail("请先解除权益与会员卡绑定");
            }
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端修改回显权益
     *
     * @param id
     * @return
     */
    @GetMapping("findByid")
    //@ApiOperation(httpMethod = "GET", value = "运营端修改回显权益")
    //@ApiImplicitParam(name = "id", value = "权益id",required=true)
    public RespResult findRights(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("权益id不能为空");
            }
            UawRights uawRights = uawRightsService.getById(id);
            return handleResult(uawRights);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端修改权益
     *
     * @param uawRights
     * @return
     */
    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "运营端修改权益类型")
    public RespResult updateRights(@RequestBody UawRights uawRights) {
        try {
            if (uawRights == null) {
                throw new AeotradeException("权益类型不能为空");
            }
            if (uawRights.getUsePattern() == 0 && StringUtils.isEmpty(uawRights.getResourceName())) {
                throw new AeotradeException("线上服务,应用地址不能为空");
            }
            uawRights.setRevision(0);
            uawRightsService.updateById(uawRights);
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }
    /**
     * 运营端添加会员卡根据权益类型id展示对应权益
     *
     * @param
     * @return
     */
    @GetMapping("findAll")
    //@ApiOperation(httpMethod = "GET", value = "运营端添加会员卡权益类型展示")
    //@ApiImplicitParam(name = "id", value = "权益id",required=true)
    public RespResult findAll(Long id){
        if(id==null){
            throw new AeotradeException("权益分类id不能为空");
        }
        UawRights uawRights=new UawRights();
        uawRights.setRightsTypeId(id);
        return handleResult(uawRightsService.lambdaQuery(uawRights).list());
    }
    /**
     *@description: 前端首页根据用户开通的会员等级查询对应的权益类型、权益

     *@return:
     *@author: wuhao
     *@date:
     */
    @GetMapping("findByClassId")
    //@ApiOperation(httpMethod = "GET", value = "前端首页根据用户开通的会员等级查询对应的权益类型、权益")
    //@ApiImplicitParam(name = "id", value = "会员类型id", required = true)
    public RespResult findByClassId(Long id){
        try {
            if(id==null){
                throw new AeotradeException("会员等级不能为空");
            }
            return handleResult(uawRightsService.findByClassid(id));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

}

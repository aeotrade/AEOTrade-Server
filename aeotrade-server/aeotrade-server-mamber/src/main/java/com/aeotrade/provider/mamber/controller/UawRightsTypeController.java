package com.aeotrade.provider.mamber.controller;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawRights;
import com.aeotrade.provider.mamber.entity.UawRightsType;
import com.aeotrade.provider.mamber.service.impl.UawRightsServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawRightsTypeServiceImpl;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UawRightsTypeController  权益类型controller
 *
 * @author admin
 */
@RestController
@RequestMapping("/uaw/RightsType/")
//@Api(tags = "权益类型")
@Slf4j
public class UawRightsTypeController extends BaseController {
    @Autowired
    private UawRightsTypeServiceImpl uawRightsTypeService;
    @Autowired
    private UawRightsServiceImpl uawRightsService;

    /**
     * 运营端列出所有的权益类型列表
     *
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "运营端列出所有的权益类型列表")
    public RespResult findPageAll(String name, @RequestParam Integer pageSize, @RequestParam Integer pageNo) {
        try {
            PageList<UawRightsType> page = uawRightsTypeService.findByName(pageSize, pageNo, name);
            return handleResultList(page);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端添加权益类型
     *
     * @param uawRightsType
     * @return
     */
    @PostMapping("insert")
    //@ApiOperation(httpMethod = "POST", value = "运营端添加权益类型")
    public RespResult insertType(@RequestBody UawRightsType uawRightsType) {
        try {
            if (uawRightsType == null) {
                throw new AeotradeException("权益类型对象不能为空");
            }
            uawRightsTypeService.save(uawRightsType);
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端删除权益类型
     *
     * @param id
     * @return RespResult
     */
    @GetMapping("delete")
   // @ApiOperation(httpMethod = "GET", value = "运营端删除权益类型")
    //@ApiImplicitParam(name = "id", value = "权益类型id", required = true)
    public RespResult deleteType(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("权益类型id不能为空");
            }
            List<UawRights> list = uawRightsService.lambdaQuery().eq(UawRights::getRightsTypeId,id)
                    .eq(UawRights::getStatus,0).list();
            if (list.size() == 0) {
                UawRightsType uawRightsType = new UawRightsType();
                uawRightsType.setId(id);
                uawRightsType.setStatus(1);
                uawRightsTypeService.updateById(uawRightsType);
            } else {
                return handleFail("请先解除权益类型与权益绑定");
            }
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端修改回显权益类型
     *
     * @param id
     * @return RespResult
     */
    @GetMapping("findByid")
    //@ApiOperation(httpMethod = "GET", value = "运营端修改回显权益类型")
   // @ApiImplicitParam(name = "id", value = "权益类型id", required = true)
    public RespResult findByid(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("权益类型id不能为空");
            }
            UawRightsType uawRightsType = new UawRightsType();
            uawRightsType.setId(id);
            return handleResult(uawRightsTypeService.lambdaQuery(uawRightsType).list());
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端修改权益类型
     *
     * @param uawRightsType
     * @return RespResult
     */
    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "运营端修改权益类型")
    public RespResult updateType(@RequestBody UawRightsType uawRightsType) {
        try {
            if (uawRightsType == null) {
                throw new AeotradeException("权益类型对象不能为空");
            }
            uawRightsType.setRevision(0);
            uawRightsTypeService.updateById(uawRightsType);
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端添加会员卡权益类型展示
     *
     * @param
     * @return
     */
    @GetMapping("findAll")
    //@ApiOperation(httpMethod = "GET", value = "运营端添加会员卡权益类型展示")
    public RespResult findAll() {
        return handleResult(uawRightsTypeService.lambdaQuery(new UawRightsType()).list());
    }

}

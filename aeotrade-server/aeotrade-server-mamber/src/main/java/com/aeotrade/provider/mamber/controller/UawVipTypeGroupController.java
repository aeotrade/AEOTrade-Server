package com.aeotrade.provider.mamber.controller;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawVipTypeGroup;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeGroupServiceImpl;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UawVipTypeGroupController 会员类型分组表 controller
 */
@RestController
@RequestMapping("/uaw/VipTypeGroup/")

@Slf4j
public class UawVipTypeGroupController extends BaseController {
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupService;

    /**
     * 运营端会员等级添加列出所有的会员分组列表
     * 前端会员套餐购买分组展示
     *
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "运营端会员等级添加列出所有的会员分组列表")
    //@ApiImplicitParam(name = "apply", value = "会员分组类型0个人1企业", required = true)
    public RespResult findAll(@RequestParam int apply) {
        return handleResult(uawVipTypeGroupService.findAll(apply));
    }

    /**
     * 运营端查询会员全部会员分组
     *
     * @return
     */
    @GetMapping("findPageAll")
    //@ApiOperation(httpMethod = "GET", value = "运营端查询会员全部会员分组及对应的工作台标识")
    public RespResult findPageAll() {
        try {
            int apply=2;
            return handleResult(uawVipTypeGroupService.findAll(apply));
        } catch (Exception e) {
            return handleFail(e);
        }
    }

    /**
     * 运营端会员分组的添加
     *
     * @return
     */
    @PostMapping("insert")
    //@ApiOperation(httpMethod = "POST", value = "运营端会员分组的添加")
    public RespResult insertVipTypeGroup(@RequestBody UawVipTypeGroup uawVipTypeGroup) {
        try {
            if (uawVipTypeGroup == null) {
                throw new AeotradeException("会员分组对象不能为空");
            }
            if (uawVipTypeGroup.getIsDefaultVip() == 1) {
                UawVipTypeGroup group = new UawVipTypeGroup();
                group.setIsDefaultVip(1);
                if (uawVipTypeGroup.getApply() == 0) {
                    group.setApply(0);
                } else {
                    group.setApply(1);
                }
                List<UawVipTypeGroup> list = uawVipTypeGroupService.lambdaQuery(group).list();
                if (list.size() != 0) {
                    throw new AeotradeException("已有默认会员分组，请删除修改上一个分组或修改当前分组");
                } else {
                    uawVipTypeGroupService.save(uawVipTypeGroup);
                    return handleOK();
                }
            } else {
                uawVipTypeGroupService.save(uawVipTypeGroup);
                return handleOK();
            }
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端会员分组修改回显
     *
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findByid")
   // @ApiOperation(httpMethod = "GET", value = "运营端会员分组修改回显")
    //@ApiImplicitParam(name = "id", value = "会员分组id", required = true)
    public RespResult findByid(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("会员分组id不能为空");
            }
            return handleResult(uawVipTypeGroupService.getById(id));
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 运营端会员分组的修改
     *
     * @return
     */
    @PostMapping("update")
    //@ApiOperation(httpMethod = "POST", value = "运营端会员分组的修改")
    public RespResult updateVipTypeGroup(@RequestBody UawVipTypeGroup uawVipTypeGroup) {
        try {
            if (uawVipTypeGroup == null) {
                throw new AeotradeException("会员分组对象不能为空");
            }
            if (uawVipTypeGroup.getIsDefaultVip() == 1) {
                UawVipTypeGroup group = new UawVipTypeGroup();
                group.setIsDefaultVip(1);
                if (uawVipTypeGroup.getApply() == 0) {
                    group.setApply(0);
                } else {
                    group.setApply(1);
                }
                List<UawVipTypeGroup> list = uawVipTypeGroupService.lambdaQuery(group).list();
                if (list.size() != 0) {
                    throw new AeotradeException("已有默认会员分组，请删除修改上一个分组或修改当前分组");
                } else {
                    uawVipTypeGroup.setRevision(0);
                    uawVipTypeGroupService.updateById(uawVipTypeGroup);
                    return handleOK();
                }
            } else {
                uawVipTypeGroup.setRevision(0);
                uawVipTypeGroupService.updateById(uawVipTypeGroup);
                return handleOK();
            }
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }
}

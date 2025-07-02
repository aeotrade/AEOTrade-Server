package com.aeotrade.provider.mamber.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.SmsHomeAdvertise;
import com.aeotrade.provider.mamber.service.SmsHomeAdvertiseService;
import com.aeotrade.provider.mamber.vo.SmsHomeAdvertiseResut;
import com.aeotrade.provider.mamber.vo.SmsHomeAdvertiseVO;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.CommonResult;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 首页轮播广告管理Controller
 * Created by hmm on 2018/11/7.
 */
@Controller
//@Api(tags = "SmsHomeAdvertiseController", description = "首页轮播广告管理")
@RequestMapping("/home/advertise")
public class SmsHomeAdvertiseController extends BaseController {
    @Autowired
    private SmsHomeAdvertiseService advertiseService;

    //@ApiOperation("添加广告")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public RespResult create(@RequestBody SmsHomeAdvertise advertise) {
        try {
            Boolean count = advertiseService.save(advertise);
            if (count) {
                return handleResult(count);
            }
        } catch (Exception e) {
            return handleResult(500, "排序字段重复,请重新输入");
        }
        return handleOK();
    }

    //@ApiOperation("删除广告")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@RequestBody Map<String, Object> ids) {

        if (ids == null || ids.get("ids") == null) {
            return CommonResult.failed("参数不能为空");
        }

        List<Long> idlongs = new ArrayList<>(1);
        Object idl = ids.get("ids");

        if (idl instanceof List) {
            idlongs = (List<Long>) idl;
        } else {
            idlongs.add(Long.valueOf(idl.toString()));
        }

        Boolean count = advertiseService.removeByIds(idlongs);
        if (count) {
            return CommonResult.success(count);
        }

        return CommonResult.failed();
    }

    //@ApiOperation("修改上下线状态")
    @RequestMapping(value = "/update/status/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateStatus(@PathVariable Long id, Integer status) {
//        int count = advertiseService.updateStatus(id, status);
        SmsHomeAdvertise byId = advertiseService.getById(id);
        byId.setStatus(status);
        boolean count = advertiseService.updateById(byId);
        if (count) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("获取广告详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<SmsHomeAdvertise> getItem(@PathVariable Long id) {
        SmsHomeAdvertise advertise = advertiseService.getById(id);
        return CommonResult.success(advertise);
    }

    //@ApiOperation("修改广告")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody SmsHomeAdvertise advertise) {
        Long aLong = advertiseService.lambdaQuery().eq(SmsHomeAdvertise::getType, advertise.getType())
                .eq(SmsHomeAdvertise::getSort, advertise.getSort()).count();
        if (aLong != 0) {
            throw new AeotradeException("排序字段重复，请重新输入");
        }
        advertise.setId(id);
        Boolean count = advertiseService.updateById(advertise);

        if (count) {
            return CommonResult.success(count);
        }

        return CommonResult.failed();
    }

    // @ApiOperation("分页查询广告")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    //@ApiParam(name = "type", value = "0->PC慧贸贸首页轮播；2->PC慧品库首页轮播；3->PC慧服务首页轮播；4->PC慧学苑首页轮播；5->PC轻应用首页轮播；1->app首页轮播", required = true)
    public CommonResult<SmsHomeAdvertiseResut> list(@RequestParam(value = "name", required = false) String name,
                                                             @RequestParam(value = "type", required = false) Integer type,
                                                       @RequestParam(value = "endTime", required = false) String endTime,
                                                       @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                       @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        SmsHomeAdvertiseResut advertiseList = advertiseService.pageList(name, type, endTime, pageSize, pageNum);
        return CommonResult.success(advertiseList);
    }
}

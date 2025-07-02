package com.aeotrade.provider.mamber.controller;


import com.aeotrade.provider.mamber.entity.PmsBrand;
import com.aeotrade.provider.mamber.service.PmsBrandService;
import com.aeotrade.suppot.CommonResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌功能Controller
 * Created by hmm on 2018/4/26.
 */
@Controller
@RequestMapping("/brand")
public class PmsBrandController {
    @Autowired
    private PmsBrandService brandService;

    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<PmsBrand>> getList() {
        return CommonResult.success(brandService.lambdaQuery().list());
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<Page<PmsBrand>> getList(@RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize) {
        LambdaQueryWrapper<PmsBrand> pmsBrandLambdaQueryChainWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotEmpty(keyword)){
            pmsBrandLambdaQueryChainWrapper.like(PmsBrand::getName,keyword);
        }
        pmsBrandLambdaQueryChainWrapper.orderByDesc(PmsBrand::getSort);
        Page<PmsBrand> page = brandService.page(new Page<>(pageNum, pageSize), pmsBrandLambdaQueryChainWrapper);
        return CommonResult.success(page);
    }

}

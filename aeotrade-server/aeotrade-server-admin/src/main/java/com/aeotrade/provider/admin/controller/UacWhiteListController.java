package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.common.CommonResult;
import com.aeotrade.provider.admin.entiy.UacWhiteList;
import com.aeotrade.provider.admin.service.UacWhiteListService;
import com.aeotrade.suppot.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 15:30
 */
@RestController
//@Api(tags = "后台白名单管理", description = "后台白名单管理")
@RequestMapping("/white")
public class UacWhiteListController extends BaseController {
    @Autowired
    private UacWhiteListService uacWhiteListService;

    //@ApiOperation(httpMethod = "GET", value = "编辑")
    @RequestMapping(value ="/update", method = RequestMethod.GET)
    public CommonResult update(String ip,Integer status){
        UacWhiteList uacWhiteList = uacWhiteListService.getById(1L);
        uacWhiteList.setStatus(status);
        if(StringUtils.isNotEmpty(ip)){
            ip.replace("，",",");
            if(isValidIPAddressMore(ip)){
                uacWhiteList.setIp(ip);
            }else{
                return CommonResult.failed("IP地址格式错误");
            }

        }else{
            uacWhiteList.setIp("");
        }
        uacWhiteListService.updateById(uacWhiteList);
        return CommonResult.success("ok");
    }

   // @ApiOperation(httpMethod = "GET", value = "查询")
    @RequestMapping(value ="/find",method = RequestMethod.GET)
    public CommonResult find(){
        UacWhiteList uacWhiteList = uacWhiteListService.getById(1L);
        return CommonResult.success(uacWhiteList);
    }

    /**
     * IP校验
     * @param str 字符串多个ip ,分割
     * @return
     */
    public static boolean isValidIPAddressMore(String str) {
        String[] ips = str.split(",");
        for (String ipAddress : ips) {
            if (Pattern.matches("^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$", ipAddress)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

}

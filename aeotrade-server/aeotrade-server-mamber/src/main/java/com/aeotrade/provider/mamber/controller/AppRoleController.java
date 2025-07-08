package com.aeotrade.provider.mamber.controller;


import com.aeotrade.provider.mamber.entity.AppCategory;
import com.aeotrade.provider.mamber.entity.AppRole;
import com.aeotrade.provider.mamber.service.AppRoleService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Auther: 吴浩
 * @Date: 2025/3/19 11:24
 */
@RestController
@RequestMapping("/app/role")
@Slf4j
public class AppRoleController extends BaseController {
    @Autowired
    private AppRoleService appRoleService;

    /**
     * 查询应用管理公开应用列表
     *
     * @param memberId
     * @param vipTypeId
     * @param categroyId
     * @param appName
     * @return
     */
    @GetMapping("/app/manage")
    public RespResult<List<AppRole>> appManage(@RequestParam String memberId, @RequestParam String vipTypeId, String categroyId, String appName) {
        List<AppRole> appRoles = appRoleService.appManage(memberId, vipTypeId, categroyId, appName);
        return handleResult(appRoles);
    }

    /**
     * 查询应用管理自建应用列表
     *
     * @param memberId
     * @param appName
     * @return
     */
    @GetMapping("/app/manage/zijian")
    public RespResult<List<AppRole>> appManageZiJian(@RequestParam String memberId, @RequestParam String vipTypeId, String appName) {
        List<AppRole> appRoles = appRoleService.appManageZiJian(memberId, vipTypeId, appName);
        return handleResult(appRoles);
    }

    /**
     * 应用根据id获取应用权限信息
     *
     * @param id
     * @return
     */
    @GetMapping("/find/Byid")
    private RespResult<AppRole> findAppRoleById(@RequestParam String id) {
        return handleResult(appRoleService.getById(id));
    }

    /**
     * 应用管理修改应用权限信息
     *
     * @param appRole
     * @return
     */
    @PostMapping("/update/app/role")
    private RespResult updateRole(@RequestBody AppRole appRole) {
        appRoleService.updateById(appRole);
        return handleOK();

    }

    /**
     * 应用中心页面根据会员类型id查询其下所有类目及其应用
     *
     * @param vipTypeId
     * @param memberId
     * @param roles
     * @return
     */
    @GetMapping("/find/vip/all")
    public RespResult<List<AppCategory>> findvipAll(@RequestParam String vipTypeId, @RequestParam String memberId, @RequestParam String roles) {
        List<AppCategory> appCategories = appRoleService.findvipAll(vipTypeId, memberId, roles);
        return handleResult(appCategories);
    }

}

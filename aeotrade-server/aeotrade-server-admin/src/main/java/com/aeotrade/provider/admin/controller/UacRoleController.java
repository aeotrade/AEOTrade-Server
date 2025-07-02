package com.aeotrade.provider.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.aeotrade.provider.admin.adminVo.InsertRoleVo;
import com.aeotrade.provider.admin.adminVo.RoleMenu;
import com.aeotrade.provider.admin.adminVo.RoleMenuDocment;
import com.aeotrade.provider.admin.common.CommonResult;
import com.aeotrade.provider.admin.entiy.UacAdminRole;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.aeotrade.provider.admin.service.UacAdminRoleService;
import com.aeotrade.provider.admin.service.UacRoleService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台用户角色管理Controller
 * Created by hmm on 2018/9/30.
 */
@RestController
//@Api(tags = "后台用户角色管理", description = "后台用户角色管理")
@RequestMapping("/role")
public class UacRoleController extends BaseController {
    @Autowired
    private UacRoleService roleService;
    @Autowired
    private UacAdminRoleService uacAdminRoleService;
    @Value("${hmtx.bi.url:}")
    private String biUrl;


    //@ApiOperation("添加角色")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult create(@RequestBody UacRole role) throws Exception {
        if (null != role.getOrgan() && role.getOrgan().equals("102") && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String, Object> map = BeanUtil.beanToMap(role);
            String httpPost = HttpRequestUtils.httpPost(biUrl + "bi_report/bi-plat-sys-roles-api/", map);
            return JSONObject.parseObject(httpPost, CommonResult.class);

        }
        if (null != role.getIsDefault() && role.getIsDefault() == 1) {
            List<UacRole> list = roleService.lambdaQuery()
                    .eq(UacRole::getIsDefault, 1)
                    .eq(UacRole::getPlatformId, role.getPlatformId())
                    .eq(UacRole::getOrgid, role.getOrgid()).list();
            if (list.size() != 0) {
                list.get(0).setIsDefault(0);
                roleService.updateById(list.get(0));
            }
        }
        role.setCreateTime(LocalDateTime.now());
        Boolean count = roleService.save(role);
        if (count) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("修改角色")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public CommonResult update(@RequestBody UacRole role) throws Exception {
        if (role.getOrgan() != null && role.getOrgan().equals("102") && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String, Object> map = BeanUtil.beanToMap(role);
            String httpPut = HttpRequestUtils.httpPut(biUrl + "bi_report/bi-plat-sys-roles-api/", map);
            return JSONObject.parseObject(httpPut, CommonResult.class);
        }
        if (null != role.getIsDefault() && role.getIsDefault() == 1) {
            List<UacRole> list = roleService.lambdaQuery()
                    .eq(UacRole::getIsDefault, 1)
                    .eq(UacRole::getPlatformId, role.getPlatformId())
                    .eq(UacRole::getOrgid, role.getOrgid()).list();
            if (list.size() != 0 && !list.get(0).getId().equals(role.getId())) {
                list.get(0).setIsDefault(0);
                roleService.updateById(list.get(0));
            }
        }
        role.setUpdatetime(LocalDateTime.now());
        Boolean count = roleService.updateById(role);
        if (count) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("批量删除角色")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@RequestParam("ids") List<Long> ids, String organ) throws Exception {
        if (ids.size() != 0 && null != organ && organ.contains("102") && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ids.get(0));
            String httpPut = HttpRequestUtils.httpDelete(biUrl + "bi_report/bi-plat-sys-roles-api/", map);
            return JSONObject.parseObject(httpPut, CommonResult.class);
        }
        for (Long id : ids) {
            UacAdminRole uacAdminRole = new UacAdminRole();
            uacAdminRole.setRoleId(id);
            List<UacAdminRole> list = uacAdminRoleService.lambdaQuery(uacAdminRole).list();
            if (list.size() != 0) {
                return CommonResult.failed("已有员工分配该角色，请先解除员工与角色的关联！");
            }
            UacRole uacRole = roleService.getById(id);
            if (uacRole.getIsDefault() == 1) {
                return CommonResult.failed("已设为默认角色不支持删除，请先关闭默认角色！");
            }
            roleService.removeById(uacRole);
        }

        return CommonResult.success(ids.size());
    }

    //@ApiOperation("获取所有角色")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    public CommonResult<List<UacRole>> listAll(@RequestParam int platform, Long memberId, @RequestParam Long platformId, Long organ) {
        UacRole uacRole = new UacRole();
        uacRole.setPlatform(platform);
        uacRole.setStatus(1);
        uacRole.setPlatformId(platformId);
        if (platform == 3) {
            uacRole.setOrgan(String.valueOf(organ));
        }
        if (null != memberId) {
            uacRole.setOrgid(String.valueOf(memberId));
        }
        List<UacRole> roleList = roleService.lambdaQuery(uacRole).list();
        return CommonResult.success(roleList);
    }


    //@ApiOperation("根据角色名称分页获取角色列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public RespResult list(@RequestParam int platform, Long memberId, String keyword, String isOpen, @RequestParam Long platformId, Long organ,
                           @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                           @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) throws Exception {
        if (null != organ && organ == 102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String, Object> map = new HashMap<>();
            map.put("platformId", platformId);
            map.put("platform", platform);
            map.put("memberId", memberId);
            map.put("isOpen", isOpen);
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("organ", organ);
            String httpGet = HttpRequestUtils.httpGet(biUrl + "bi_report/bi-plat-sys-roles-api/", map);
            return JSONObject.parseObject(httpGet, RespResult.class);
        }
        LambdaQueryWrapper<UacRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UacRole::getPlatform, platform);
        queryWrapper.eq(UacRole::getPlatformId, platformId);
        if (null != memberId) {
            queryWrapper.eq(UacRole::getOrgid, String.valueOf(memberId));
        }
        if (StringUtils.isNotEmpty(keyword)) {
            queryWrapper.like(UacRole::getName, keyword);
        }
        if (platform == 3) {
            queryWrapper.eq(UacRole::getOrgan, String.valueOf(organ));
        }
        if (StringUtils.isEmpty(isOpen) && memberId == 0) {
            queryWrapper.eq(UacRole::getStatus, 1);
        }
        Page<UacRole> page = roleService.page(new Page<>(pageNum, pageSize), queryWrapper);
        PageList<UacRole> list = new PageList<>();
        list.setRecords(page.getRecords());
        list.setTotalSize(page.getTotal());
        return handleResultList(list);
    }

    //@ApiOperation("修改角色状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.GET)
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        UacRole uacRole = roleService.getById(id);
        if (null == uacRole) {
            return CommonResult.failed();
        }
        UacAdminRole uacAdminRole = new UacAdminRole();
        uacAdminRole.setRoleId(id);
        List<UacAdminRole> list = uacAdminRoleService.lambdaQuery(uacAdminRole).list();
        if (list.size() != 0 && status == 0) {
            return CommonResult.failed("禁用角色请解除与用户的绑定");
        }
        uacRole.setStatus(status);
        Boolean count = roleService.updateById(uacRole);
        if (count) {
            return CommonResult.success(count);
        }
        return CommonResult.failed();
    }

    //@ApiOperation("获取角色相关菜单")
    @RequestMapping(value = "/listMenu/{roleId}", method = RequestMethod.GET)
    public CommonResult listMenu(@PathVariable Long roleId) throws Exception {
        RoleMenuDocment roleMenuDocment = roleService.listMenu(roleId);
        return CommonResult.success(roleMenuDocment);
    }

    //@ApiOperation("给角色分配菜单")
    @RequestMapping(value = "/allocMenu", method = RequestMethod.POST)
    public CommonResult allocMenu(@RequestBody RoleMenu roleMenu) throws Exception {
        if (roleMenu.getMenuIds().size() == 0) {
            return CommonResult.failed("角色菜单不能为空");
        }
        if (null != roleMenu.getOrgan() && roleMenu.getOrgan() == 102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String, Object> map = BeanUtil.beanToMap(roleMenu);
            map.put("permissions", map.get("menuIds"));
            map.put("id", map.get("roleId"));
            String httpGet = HttpRequestUtils.httpPut(biUrl + "bi_report/bi-plat-sys-roles-api/", map);
            return JSONObject.parseObject(httpGet, CommonResult.class);
        }
        int count = roleService.allocMenu(roleMenu.getRoleId(), roleMenu.getMenuIds(), roleMenu.getUacRoleDocments());
        return CommonResult.success(count);
    }


    //@ApiOperation("运营平台查询企业自建角色")
    @RequestMapping(value = "/member/role", method = RequestMethod.GET)
    public CommonResult memberRole(@RequestParam Long platformId, @RequestParam Long memberId, Long organ) {
        return CommonResult.success(roleService.memberRole(platformId, memberId, organ));
    }

    //@ApiOperation("获取所有角色并将角色和菜单添加到redis中")
    @RequestMapping(value = "/role/redis", method = RequestMethod.GET)
    public CommonResult RoleRedis(Long platformId) {
        roleService.RoleRedis(platformId);
        return CommonResult.success("ok");
    }

    //@ApiOperation("保管箱创建角色并给角色分配菜单")
    @RequestMapping(value = "/insert/role", method = RequestMethod.POST)
    public CommonResult insertRole(@RequestBody InsertRoleVo insertRoleVo) throws Exception {
        if (insertRoleVo.getMenuIds().size() == 0) {
            return CommonResult.failed("角色菜单不能为空");
        }
        UacRole uacRole = new UacRole();
        uacRole.setName(insertRoleVo.getName());
        uacRole.setDescription(insertRoleVo.getDescription());
        uacRole.setIsModel(0);
        uacRole.setOrgan(insertRoleVo.getOrgan());
        uacRole.setOrgid(insertRoleVo.getOrgid());
        uacRole.setPlatform(insertRoleVo.getPlatform());
        uacRole.setPlatformId(insertRoleVo.getPlatformId());
        uacRole.setStatus(insertRoleVo.getStatus());
        uacRole.setIsDefault(0);
        uacRole.setCreateTime(LocalDateTime.now());
        roleService.save(uacRole);
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(String.valueOf(uacRole.getId()));
        roleMenu.setMenuIds(insertRoleVo.getMenuIds());
        roleMenu.setUacRoleDocments(insertRoleVo.getUacRoleDocments());
        allocMenu(roleMenu);
        return CommonResult.success(uacRole);
    }

    @RequestMapping(value = "/update/role", method = RequestMethod.POST)
    public CommonResult updateRole(@RequestBody InsertRoleVo insertRoleVo) throws Exception {
        if (insertRoleVo.getMenuIds().size() == 0) {
            return CommonResult.failed("角色菜单不能为空");
        }
        UacRole uacRole = new UacRole();
        uacRole.setId(insertRoleVo.getId());
        uacRole.setName(insertRoleVo.getName());
        uacRole.setDescription(insertRoleVo.getDescription());
        uacRole.setIsModel(0);
        uacRole.setOrgan(insertRoleVo.getOrgan());
        uacRole.setOrgid(insertRoleVo.getOrgid());
        uacRole.setPlatform(insertRoleVo.getPlatform());
        uacRole.setPlatformId(insertRoleVo.getPlatformId());
        uacRole.setStatus(insertRoleVo.getStatus());
        uacRole.setIsDefault(0);
        uacRole.setCreateTime(LocalDateTime.now());
        roleService.updateById(uacRole);
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(String.valueOf(insertRoleVo.getId()));
        roleMenu.setMenuIds(insertRoleVo.getMenuIds());
        roleMenu.setUacRoleDocments(insertRoleVo.getUacRoleDocments());
        allocMenu(roleMenu);
        return CommonResult.success(uacRole);
    }

    @RequestMapping(value = "/update/defa/role", method = RequestMethod.GET)
    public CommonResult updateDefaRole(String roleIds, @RequestParam Long memberId, @RequestParam Long platform) {
        List<UacRole> list = roleService.lambdaQuery().eq(UacRole::getOrgid, memberId).eq(UacRole::getPlatform, platform).list();
        for (UacRole uacRole : list) {
            uacRole.setIsDefault(0);
            roleService.updateById(uacRole);
        }
        if (StringUtils.isNotEmpty(roleIds)) {
            String[] split = roleIds.split(",");
            for (String s : split) {
                UacRole byId = roleService.getById(s);
                if (null != byId) {
                    byId.setIsDefault(1);
                    roleService.updateById(byId);
                }
            }
        }
        return CommonResult.success("ok");
    }

}

package com.aeotrade.provider.admin.controller;

import com.aeotrade.provider.admin.adminVo.UacMenuNode;
import com.aeotrade.provider.admin.common.CommonPage;
import com.aeotrade.provider.admin.common.CommonResult;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import com.aeotrade.provider.admin.service.UawWorkbenchMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台菜单管理Controller
 */
@RestController
////@Api(tags = "后台菜单管理", description = "后台菜单管理")
@RequestMapping("/menu")
public class UacMenuController {

    @Autowired
    private UawWorkbenchMenuService uawWorkbenchMenuService;

    ////@ApiOperation("添加后台菜单")
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult create(@RequestBody UawWorkbenchMenu uacMenu) {
        UawWorkbenchMenu menu = uawWorkbenchMenuService.getMenu(uacMenu.getParentId(),uacMenu.getSort());
        if (menu != null) {
            return CommonResult.failed("顺序不能相同");
        } else {
            int count = uawWorkbenchMenuService.create(uacMenu);
            if (count > 0) {
                return CommonResult.success(count);
            } else {
                return CommonResult.failed();
            }
        }
    }

    //@ApiOperation("批量添加后台菜单")
    @RequestMapping(value = "/create/all", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('menu:add')")
    public CommonResult createall(@RequestBody List<UawWorkbenchMenu> uacMenus) {
        for (UawWorkbenchMenu uacMenu : uacMenus) {
            int count = uawWorkbenchMenuService.create(uacMenu);
        }
        return CommonResult.success("ok");
    }

    //@ApiOperation("修改后台菜单")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id,
                               @RequestBody UawWorkbenchMenu UacMenu) {
        UawWorkbenchMenu menu = uawWorkbenchMenuService.getMenu(UacMenu.getParentId(),UacMenu.getSort());

        UawWorkbenchMenu menu1 = uawWorkbenchMenuService.getById(id);
        if (menu != null && !menu.getSort().equals(menu1.getSort())) {
            return CommonResult.failed("顺序不能相同");
        } else {
            int count = uawWorkbenchMenuService.updateMenu(id, UacMenu);
            if (count > 0) {
                return CommonResult.success(count);
            } else {
                return CommonResult.failed();
            }
        }
    }

    //@ApiOperation("根据ID获取菜单详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public CommonResult<UawWorkbenchMenu> getItem(@PathVariable Long id) {

        UawWorkbenchMenu uacMenu = uawWorkbenchMenuService.getById(id);
//        System.out.println(uacMenu);
//        UawWorkbenchMenu UacMenu = menuService.getItem(id);
        return CommonResult.success(uacMenu);
    }

    //@ApiOperation("根据ID删除后台菜单")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        Boolean count = uawWorkbenchMenuService.removeById(id);
        if (count) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    //@ApiOperation("分页查询后台菜单")
    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    public CommonResult<CommonPage<UawWorkbenchMenu>> list(@PathVariable Long parentId,
                                                        @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                        @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<UawWorkbenchMenu> menuList = uawWorkbenchMenuService.findlist(parentId, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(menuList));
    }

    //@ApiOperation("树形结构返回所有菜单列表")
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    public CommonResult<List<UacMenuNode>> treeList() {
        List<UacMenuNode> list = uawWorkbenchMenuService.treeList();
        return CommonResult.success(list);
    }


    //@ApiOperation("修改菜单显示状态")
    @RequestMapping(value = "/updateHidden/{id}", method = RequestMethod.POST)
    public CommonResult updateHidden(@PathVariable Long id, @RequestParam("hidden") Integer hidden) {
        int count = uawWorkbenchMenuService.updateHidden(id, hidden);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

    //@ApiOperation("树形结构返回所有菜单列表指定用户的角色菜单")
    @RequestMapping(value = "/list/user/{id}", method = RequestMethod.GET)
    public CommonResult<List<UacMenuNode>> treeList(@PathVariable Long id) {
        List<UacMenuNode> list = uawWorkbenchMenuService.ListMenuUser(id);
        return CommonResult.success(list);
    }


}

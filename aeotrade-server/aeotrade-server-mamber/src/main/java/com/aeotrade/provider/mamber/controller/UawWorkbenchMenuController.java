package com.aeotrade.provider.mamber.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.UawVipMessage;
import com.aeotrade.provider.mamber.entity.UawVipType;
import com.aeotrade.provider.mamber.entity.UawWorkbench;
import com.aeotrade.provider.mamber.entity.UawWorkbenchMenu;
import com.aeotrade.provider.mamber.service.UawWorkbenchService;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawWorkbenchMenuServiceImpl;
import com.aeotrade.provider.mamber.vo.WorkbenchVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.CommonResult;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.HttpRequestUtils;
import com.aeotrade.utlis.ThreadPoolUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UawWorkbenchController 工作台 controller
 *
 * @author wuhao
 */
@RestController
@RequestMapping("/uaw/WorkbenchMenu/")
@Slf4j
public class UawWorkbenchMenuController extends BaseController {
    @Autowired
    private UawWorkbenchMenuServiceImpl uawWorkbenchMenuService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawWorkbenchService uawWorkbenchService;

    @Value("${hmtx.bi.url:}")
    private String biUrl;

    /**
     * 根据工作台id查询菜单
     *
     * @returnupdateMenu
     */
    @GetMapping("find/menu")
    //@ApiOperation(httpMethod = "GET", value = "根据工作台id查询菜单")
    //@ApiImplicitParam(name = "id", value = "工作台id", required = true)
    public RespResult findbyWorkbenchId(@RequestParam Long id, Long memberId, @RequestParam Long workbenchId) {
        try {
            if (id == null) {
                throw new AeotradeException("工作台id不能为空");
            }
            List<WorkbenchVo> list = uawWorkbenchMenuService.findbyMenu(id, memberId, workbenchId);
            if (memberId != null) {
                UawVipMessage uawVipMessage = new UawVipMessage();
                uawVipMessage.setMemberId(memberId);
                uawVipMessage.setUserType(1);
                uawVipMessage.setVipStatus(0);
                uawVipMessage.setStatus(0);
                List<UawVipMessage> uawVipMessagelist = uawVipMessageService.lambdaQuery(uawVipMessage).list();
                if (uawVipMessagelist.size() != 0) {
                    for (UawVipMessage vipMessage : uawVipMessagelist) {
                        UawVipType uawVipType = uawVipTypeService.getById(vipMessage.getTypeId());
                        if (uawVipType.getWorkbench().equals(workbenchId)) {
                            if (vipMessage.getVipStatus() == 0) {
                                for (int i = 0; i < list.size(); i++) {
                                    if (i != 0) {
                                        if (!"应用".equals(list.get(i).getName())) {
                                            list.get(i).setChildren(null);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        uawWorkbenchMenuService.redisMenuAll();
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            return handleResult(list);
        } catch (Exception e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 根据工作台id查询菜单
     *
     * @return
     */
    @GetMapping("findbyWorkbenchId")
    //@ApiOperation(httpMethod = "GET", value = "根据工作台id查询菜单")
   // @ApiImplicitParam(name = "id", value = "工作台id", required = true)
    public RespResult findbyWorkbenchId(@RequestParam Long id, @RequestParam int type) throws Exception {
        try {
            if (id == null) {
                throw new AeotradeException("工作台id不能为空");
            }
            if(type==3){
                UawWorkbench uawWorkbench = uawWorkbenchService.getById(id);
                if(null!=uawWorkbench && uawWorkbench.getChannelColumnsId()==102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
                    Map<String,Object> map=new HashMap<>();
                    map.put("id",id);
                    map.put("type",type);
                    String httpGet = HttpRequestUtils.httpGet(biUrl + "/bi_report/bi-dict-perm-api/", map);
                    return JSONObject.parseObject(httpGet, RespResult.class);
                }
            }
            List<WorkbenchVo> list = uawWorkbenchMenuService.findbyWorkbenchId(id, type);
            return handleResult(list);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * 根据工作台id查询菜单是否默认分开显示
     *
     * @return
     */
    @GetMapping("findbyDefault")
   // @ApiOperation(httpMethod = "GET", value = "根据工作台id查询菜单")
    //@ApiImplicitParam(name = "id", value = "工作台id", required = true)
    public RespResult findbyIsDefault(@RequestParam Long id, @RequestParam int type, @RequestParam Long memberId) throws Exception {
        try {
            if (id == null) {
                throw new AeotradeException("工作台id不能为空");
            }
            if(type==3){
                UawWorkbench uawWorkbench = uawWorkbenchService.getById(id);
                if(null!=uawWorkbench && uawWorkbench.getChannelColumnsId()==102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
                    Map<String,Object> map=new HashMap<>();
                    map.put("id",id);
                    map.put("type",type);
                    map.put("memberId",0);
                    String httpGet = HttpRequestUtils.httpGet(biUrl + "/bi_report/bi-plat-roles-api/", map);
                    return JSONObject.parseObject(httpGet, RespResult.class);
                }
            }
            List<WorkbenchVo> list = uawWorkbenchMenuService.findbyDefault(id, type, memberId);
            return handleResult(list);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @GetMapping("find/menu/parentId")
   // @ApiOperation(httpMethod = "GET", value = "分页查询对应平台菜单")
    public RespResult findMenu(@RequestParam Long parentId, @RequestParam int type, @RequestParam Long workBenchId,
                               @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                               @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        try {
            PageList<UawWorkbenchMenu> menu = uawWorkbenchMenuService.findMenu(parentId, type, workBenchId, pageSize, pageNum);
            return handleResultList(menu);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @PostMapping("insertMenu")
   // @ApiOperation(httpMethod = "POST", value = "工作台菜单的添加")
    public RespResult insertMenu(@RequestBody UawWorkbenchMenu uawWorkbenchMenu) throws Exception {
        try {
            if (uawWorkbenchMenu == null) {
                throw new AeotradeException("工作台菜单对象不能为空");
            }
            UawWorkbench uawWorkbench = uawWorkbenchService.getById(uawWorkbenchMenu.getWorkbenchId());
            if(null!=uawWorkbench && uawWorkbench.getChannelColumnsId()==102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
                Map<String,Object> map= BeanUtil.beanToMap(uawWorkbenchMenu);
                String httpGet = HttpRequestUtils.httpPost(biUrl + "/bi_report/bi-dict-perm-api/", map);
                return JSONObject.parseObject(httpGet, RespResult.class);
            }
            if (null == uawWorkbenchMenu.getParentId()) {
                uawWorkbenchMenu.setParentId(0L);
            }
            Boolean insert = uawWorkbenchMenuService.save(uawWorkbenchMenu);
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        uawWorkbenchMenuService.redisMenuAll();
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            return handleResult(insert);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 工作台菜单修改回显
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findbyId")
    //@ApiOperation(httpMethod = "GET", value = "工作台菜单修改回显")
    //@ApiImplicitParam(name = "id", value = "菜单id", required = true)
    public RespResult findById(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("菜单id不能为空");
            }
            UawWorkbenchMenu uawWorkbenchMenu = uawWorkbenchMenuService.getById(id);
            return handleResult(uawWorkbenchMenu);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @PostMapping("updateMenu")
    //@ApiOperation(httpMethod = "POST", value = "工作台菜单的修改")
    public RespResult updateMenu(@RequestBody UawWorkbenchMenu uawWorkbenchMenu) throws Exception {
        try {
            if (uawWorkbenchMenu == null) {
                throw new AeotradeException("工作台菜单对象不能为空");
            }
            UawWorkbench uawWorkbench = uawWorkbenchService.getById(uawWorkbenchMenu.getWorkbenchId());
            if(null!=uawWorkbench && uawWorkbench.getChannelColumnsId()==102 && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
                Map<String,Object> map= BeanUtil.beanToMap(uawWorkbenchMenu);
                String httpPut = HttpRequestUtils.httpPut(biUrl + "/bi_report/bi-dict-perm-api/", map);
                return JSONObject.parseObject(httpPut, RespResult.class);
            }
            if (null == uawWorkbenchMenu.getParentId()) {
                uawWorkbenchMenu.setParentId(0L);
            }
            uawWorkbenchMenu.setRevision(0);
            Boolean insert = uawWorkbenchMenuService.updateById(uawWorkbenchMenu);
            ThreadPoolUtils.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        uawWorkbenchMenuService.redisMenuAll();
                    } catch (Exception e) {
                        log.warn(e.getMessage());
                    }
                }
            });
            return handleResult(insert);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @GetMapping("update/hidden/{id}")
    //@ApiOperation(httpMethod = "GET", value = "工作台菜单的修改")
    public RespResult updatehidden(@PathVariable Long id, @RequestParam int hidden) {
        UawWorkbenchMenu uawWorkbenchMenu = uawWorkbenchMenuService.getById(id);
        if (null != uawWorkbenchMenu) {
            List<UawWorkbenchMenu> uawWorkbenchMenus = new ArrayList<UawWorkbenchMenu>();
            uawWorkbenchMenus.add(uawWorkbenchMenu);
            List<UawWorkbenchMenu> menus = searchSubNotActiveMenu(uawWorkbenchMenus, uawWorkbenchMenu.getId());
            for (UawWorkbenchMenu workbenchMenu : menus) {
                workbenchMenu.setIsHidden(hidden);
                uawWorkbenchMenuService.updateById(workbenchMenu);
            }
        }
        return handleOK();
    }

    @GetMapping("update/isdefalt/{id}")
    //@ApiOperation(httpMethod = "GET", value = "工作台菜单的修改")
    public RespResult updatedefalt(@PathVariable Long id, @RequestParam int isdefalt) {
        UawWorkbenchMenu uawWorkbenchMenu = uawWorkbenchMenuService.getById(id);
        if (null != uawWorkbenchMenu) {
            List<UawWorkbenchMenu> uawWorkbenchMenus = new ArrayList<>();
            uawWorkbenchMenus.add(uawWorkbenchMenu);
            List<UawWorkbenchMenu> menus = searchSubNotActiveMenu(uawWorkbenchMenus, uawWorkbenchMenu.getId());
            for (UawWorkbenchMenu workbenchMenu : menus) {
                workbenchMenu.setIsDefault(isdefalt);
                uawWorkbenchMenuService.updateById(workbenchMenu);
            }
            if (isdefalt == 1) {
                uawWorkbenchMenuService.updateVipClassMenu(uawWorkbenchMenu.getWorkbenchId(), menus);
            }
        }
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    uawWorkbenchMenuService.redisMenuAll();
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
        return handleOK();
    }

    public List<UawWorkbenchMenu> searchSubNotActiveMenu(List<UawWorkbenchMenu> uawWorkbenchMenus, Long parentId) {
        UawWorkbenchMenu uawWorkbenchMenu = new UawWorkbenchMenu();
        uawWorkbenchMenu.setParentId(parentId);
        List<UawWorkbenchMenu> list = uawWorkbenchMenuService.lambdaQuery(uawWorkbenchMenu).list();
        if (list != null && list.size() > 0) {
            for (UawWorkbenchMenu data : list) {
                uawWorkbenchMenus.add(data);
                searchSubNotActiveMenu(uawWorkbenchMenus, data.getId());
            }
        }

        return uawWorkbenchMenus;
    }

    /**
     * @description: 工作台菜单删除
     * @return:
     * @author: wuhao
     * @date:
     */
   // @ApiOperation("根据ID删除后台菜单")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RespResult delete(@PathVariable Long id,@RequestBody String organ) throws Exception {
        if(StringUtils.isNotEmpty(organ) && organ.contains("102") && !org.springframework.util.StringUtils.isEmpty(biUrl)) {
            Map<String,Object> map= new HashMap<>();
            map.put("id",id);
            String httpDelete = HttpRequestUtils.httpDelete(biUrl + "/bi_report/bi-dict-perm-api/", map);
            return JSONObject.parseObject(httpDelete, RespResult.class);
        }
        uawWorkbenchMenuService.delete(id);
        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    uawWorkbenchMenuService.redisMenuAll();
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
            }
        });
        return handleOK();
    }

    /**
     * @description: 获取企业已有的菜单权限
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("find/member/menu")
   // @ApiOperation(httpMethod = "GET", value = "获取企业已有的菜单权限")
    public RespResult findMemberMenu(@RequestParam Long memberId, @RequestParam Long type) {
        try {
            if (memberId == null) {
                throw new AeotradeException("企业id不能为空");
            }
            if (type == null) {
                throw new AeotradeException("菜单类型id不能为空");
            }
            List<WorkbenchVo> list = uawWorkbenchMenuService.findMemberMenu(memberId, type);
            return handleResult(list);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    @GetMapping("get/menu/all")
   // @ApiOperation(httpMethod = "GET", value = "将所有资源存放到redis")
    public RespResult redisMenuAll() {
        try {
            uawWorkbenchMenuService.redisMenuAll();
            return handleOK();
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }


}

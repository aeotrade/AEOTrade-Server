package com.aeotrade.provider.mamber.controller;


import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.service.impl.UawVipMessageServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawVipTypeServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawWorkbenchMenuServiceImpl;
import com.aeotrade.provider.mamber.service.impl.UawWorkbenchServiceImpl;
import com.aeotrade.provider.mamber.vo.WorkbenchTypeVo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * UawWorkbenchController 工作台 controller
 *
 * @author wuhao
 */
@RestController
@RequestMapping("/uaw/Workbench/")
@Slf4j
public class UawWorkbenchController extends BaseController {

    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchService;
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawWorkbenchMenuServiceImpl uawWorkbenchMenuService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;

    /**
     * 运营端查询全部工作台
     *
     * @return
     */
    @GetMapping("list")
    //@ApiOperation(httpMethod = "GET", value = "运营端查询全部工作台（分页）")
    //@ApiImplicitParams({
           // @ApiImplicitParam(name = "pageSize", value = "每页多少条", defaultValue = "10", required = true),
           // @ApiImplicitParam(name = "pageNo", value = "当前要查询的页码", defaultValue = "1", required = true)})
    public RespResult findpageAll(@RequestParam Integer pageSize, @RequestParam Integer pageNo, Integer type) {
        LambdaQueryWrapper<UawWorkbench> uawWorkbenchLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (type!=0) {
            uawWorkbenchLambdaQueryWrapper.eq(UawWorkbench::getPlatformType, type);
        }
        uawWorkbenchLambdaQueryWrapper.eq(UawWorkbench::getStatus, 0);
        //调用分页方法进行权益项查询
        Page<UawWorkbench> page = uawWorkbenchService.page(new Page(pageNo,pageSize), uawWorkbenchLambdaQueryWrapper);
        PageList<UawWorkbench> list=new PageList<>();
        list.setRecords(page.getRecords());
        list.setTotalSize(page.getTotal());
        return handleResultList(list);
    }

    /**
     * @description: 运营端查询全部工作台
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findAll")
   // @ApiOperation(httpMethod = "GET", value = "运营端查询全部工作台")
    //@ApiImplicitParams({
           // @ApiImplicitParam(name = "type", value = "所属平台标识", defaultValue = "0", required = true)})
    public RespResult findAll(@RequestParam Integer type) {
        UawWorkbench uawWorkbench = new UawWorkbench();
        if (type != 0) {
            uawWorkbench.setPlatformType(type);
        }
        List<UawWorkbench> list = uawWorkbenchService.lambdaQuery(uawWorkbench).list();
        List<WorkbenchTypeVo> workbenchTypeVos=new ArrayList<>();
        for (UawWorkbench workbench : list) {
            WorkbenchTypeVo workbenchTypeVo=new WorkbenchTypeVo();
            BeanUtils.copyProperties(workbench,workbenchTypeVo);
            workbenchTypeVo.setIsChoice(0);
            if(null!=workbench.getId()){
                UawVipType uawVipType=new UawVipType();
                uawVipType.setWorkbench(workbench.getId());
                List<UawVipType> vipTypes = uawVipTypeService.lambdaQuery(uawVipType).list();
                if(!vipTypes.isEmpty()){
                    workbenchTypeVo.setIsChoice(1);
                }
            }
            workbenchTypeVos.add(workbenchTypeVo);
        }
        return handleResult(workbenchTypeVos);
    }

    /**
     * @description: 运营端添加工作台
     * @return:
     * @author: wuhao
     * @date:
     */
    @PostMapping("insertworkbench")
   // @ApiOperation(httpMethod = "POST", value = "工作台的添加")
    public RespResult insertWorkbench(@RequestBody UawWorkbench uawWorkbench) {
        try {
            if (uawWorkbench == null) {
                throw new AeotradeException("工作台对象不能为空");
            }
            uawWorkbench.setCreatedTime(LocalDateTime.now());
            uawWorkbench.setWorkbenchStatus(SgsConstant.TypeStatus.NO.getValue());
            boolean insert = uawWorkbenchService.save(uawWorkbench);
            return handleResult(insert);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端工作台修改回显
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("findByid")
   // @ApiOperation(httpMethod = "GET", value = "运营端工作台修改回显")
   // @ApiImplicitParam(name = "id", value = "工作台id", required = true)
    public RespResult findByid(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("工作台id不能未空");
            }
            UawWorkbench uawWorkbench = uawWorkbenchService.getById(id);
            return handleResult(uawWorkbench);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端修改工作台
     * @return:
     * @author: wuhao
     * @date:
     */
    @PostMapping("updateworkbench")
   // @ApiOperation(httpMethod = "POST", value = "工作台的修改")
    public RespResult updateWorkbench(@RequestBody UawWorkbench uawWorkbench) {
        try {
            if (uawWorkbench == null) {
                throw new AeotradeException("工作台对象不能为空");
            }
            uawWorkbench.setUpdatedTime(LocalDateTime.now());
            uawWorkbench.setRevision(0);
            Boolean update = uawWorkbenchService.updateById(uawWorkbench);
            UawWorkbenchMenu uawWorkbenchMenu=new UawWorkbenchMenu();
            uawWorkbenchMenu.setWorkbenchId(uawWorkbench.getId());
            for (UawWorkbenchMenu workbenchMenu : uawWorkbenchMenuService.lambdaQuery(uawWorkbenchMenu).list()) {
                workbenchMenu.setPlatformType(uawWorkbench.getPlatformType());
                uawWorkbenchMenuService.updateById(workbenchMenu);
            }
            return handleResult(update);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    /**
     * @description: 运营端工作台删除
     * @return:
     * @author: wuhao
     * @date:
     */
    @GetMapping("deleteByid")
    //@ApiOperation(httpMethod = "GET", value = "运营端工作台删除")
    //@ApiImplicitParam(name = "id", value = "工作台id", required = true)
    public RespResult deleteByid(@RequestParam Long id) {
        try {
            if (id == null) {
                throw new AeotradeException("工作台id不能未空");
            }
            UawWorkbenchMenu uawWorkbenchMenu = new UawWorkbenchMenu();
            uawWorkbenchMenu.setWorkbenchId(id);
            List<UawWorkbenchMenu> list = uawWorkbenchMenuService.lambdaQuery(uawWorkbenchMenu).list();
            if (!list.isEmpty()) {
                throw new AeotradeException("工作台有关联的菜单，请删除关联菜单后删除工作台");
            }
            UawVipType uawVipType = new UawVipType();
            uawVipType.setWorkbench(id);
            List<UawVipType> typeList = uawVipTypeService.lambdaQuery(uawVipType).list();
            if (!typeList.isEmpty()) {
                throw new AeotradeException("该工作台正在使用中不能删除");
            }
            Boolean i = uawWorkbenchService.removeById(id);
            return handleResult(i);
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            return handleFail(e);
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "用户添加默认工作台")
    @GetMapping("/insert/last")
    public RespResult insertLast(@RequestParam("staffId") Long staffId, @RequestParam("memberId") Long memberId, @RequestParam("workBenchId") Long workBenchId) {
        if (null == staffId) {
            throw new AeotradeException("用户ID不能为空");
        }
        if (null == workBenchId) {
            throw new AeotradeException("工作台ID不能为空");
        }
        uawWorkbenchService.updateDefultWorkbench(staffId, memberId, workBenchId);
        return handleOK();
    }

   // @ApiOperation(httpMethod = "GET", value = "用户获取默认工作台")
    @GetMapping("/get/redis")
    public RespResult getRedis(@RequestParam("staffId") Long staffId, @RequestParam("memberId") Long memberId) {
        try {
            if (null == memberId) {
                throw new AeotradeException("用户ID不能为空");
            }
                UawVipMessage uawVipMessage = new UawVipMessage();
                uawVipMessage.setMemberId(memberId);
                uawVipMessage.setUserType(1);
                List<UawVipMessage> list = uawVipMessageService.lambdaQuery(uawVipMessage).list();
                if(CommonUtil.isEmpty(list) && null!= staffId){
                    uawVipMessage.setMemberId(null);
                    uawVipMessage.setUserType(0);
                    uawVipMessage.setStaffId(staffId);
                    List<UawVipMessage> vips = uawVipMessageService.lambdaQuery(uawVipMessage).list();
                    if(CommonUtil.isEmpty(vips)){
                        return handleResult(1L);
                    }
                    UawVipType types =uawVipTypeService.getById(vips.get(0).getTypeId());
                    if(null==types || types.getWorkbench() ==null){
                        return handleResult(1L);
                    }
                    return handleResult(types.getWorkbench());
                }
                if(CommonUtil.isEmpty(list) && null!= staffId){
                    return handleResult(1L);
                }
                UawVipType uawVipType =uawVipTypeService.getById(list.get(0).getTypeId());
                return handleResult(uawVipType.getWorkbench());
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            throw new AeotradeException("获取默认工作台失败");
        }
    }


   // @ApiOperation(httpMethod = "GET", value = "根据类型查询对应工作台")
    @GetMapping("/get/workbench")
    public RespResult getRedis(@RequestParam int type) {
        try {
            UawWorkbench uawWorkbench=new UawWorkbench();
            uawWorkbench.setPlatformType(type);
            return handleResult(uawWorkbenchService.lambdaQuery(uawWorkbench).list());
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            throw new AeotradeException("获取默认工作台失败");
        }
    }

    //@ApiOperation(httpMethod = "GET", value = "根据频道栏目id查询对应工作台")
    @GetMapping("/get/appid")
    public RespResult getByAPPiD(@RequestParam Long id) {
        try {
            UawWorkbench uawWorkbench=new UawWorkbench();
            uawWorkbench.setChannelColumnsId(id);
            return handleResult(uawWorkbenchService.lambdaQuery(uawWorkbench).list());
        } catch (AeotradeException e) {
            log.warn(e.getMessage());
            throw new AeotradeException("获取默认工作台失败");
        }
    }

}

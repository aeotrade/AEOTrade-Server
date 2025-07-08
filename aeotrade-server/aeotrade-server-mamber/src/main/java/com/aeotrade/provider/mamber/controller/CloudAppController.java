package com.aeotrade.provider.mamber.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.AppCategory;
import com.aeotrade.provider.mamber.entity.AppCloud;
import com.aeotrade.provider.mamber.entity.AppVipType;
import com.aeotrade.provider.mamber.entity.UacMember;
import com.aeotrade.provider.mamber.service.AppCategoryService;
import com.aeotrade.provider.mamber.service.AppCloudService;
import com.aeotrade.provider.mamber.service.AppVipTypeService;
import com.aeotrade.provider.mamber.service.UacMemberService;
import com.aeotrade.provider.mamber.vo.AppCateSort;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.PageList;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/app")
@Slf4j
public class CloudAppController extends BaseController {
    @Autowired
    private AppCloudService appCloudService;
    @Autowired
    private AppVipTypeService appVipTypeService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private AppCategoryService appCategoryService;


    /**
     * 应用和推荐服务添加
     * @param appCloud
     * @return
     */
    @PostMapping("/save")
    public RespResult saveApp(@RequestBody AppCloud appCloud) {
        appCloud.setCreatedTime(LocalDateTime.now());
        appCloud.setUpdatedTime(LocalDateTime.now());
        appCloud.setRevision(0);
        appCloud.setStatus(0);
        appCloudService.save(appCloud);
        for (AppVipType appVipType : appCloud.getVipTypeId()) {
            appVipType.setCloudId(appCloud.getId());
            appVipTypeService.save(appVipType);
        }
        return handleOK();
    }

    /**
     * 应用和推荐服务修改
     * @param appCloud
     * @return
     */
    @PostMapping("/update")
    public RespResult updateApp(@RequestBody AppCloud appCloud) {
        appCloud.setUpdatedTime(LocalDateTime.now());
        appCloudService.updateById(appCloud);
        appVipTypeService.removeByIds(appVipTypeService.lambdaQuery().eq(AppVipType::getCloudId, appCloud.getId()).list());
        for (AppVipType appVipType : appCloud.getVipTypeId()) {
            appVipType.setCloudId(appCloud.getId());
            appVipTypeService.save(appVipType);
        }
        return handleOK();
    }

    /**
     * 根据应用id查询应用详情
     * @param appId
     * @return
     * @throws Exception
     */
    @GetMapping("/find/byid")
    public RespResult<AppCloud> findByid(@RequestParam String appId) throws Exception {
        AppCloud appCloud = appCloudService.getById(appId);
        List<AppVipType> vipTypes = appVipTypeService.lambdaQuery().eq(AppVipType::getCloudId, appCloud.getId()).list();
        appCloud.setVipTypeId(vipTypes);
        return handleResult(appCloud);
    }


    /**
     * 根据应用id删除应用
     * @param appId
     * @return
     * @throws Exception
     */
    @GetMapping("/delete/byid")
    public RespResult<AppCloud> deleteApp(@RequestParam String appId) {
        AppCloud appCloud = appCloudService.getById(appId);
        if (appCloud.getPublishStatus() != 2) {
            throw new AeotradeException("未下架应用不允许删除");
        }
        appCloudService.removeById(appId);
        List<AppVipType> vipTypes = appVipTypeService.lambdaQuery().eq(AppVipType::getCloudId, appId).list();
        appVipTypeService.removeByIds(vipTypes);
        return handleOK();
    }

    /**
     * 开放平台查询企业创建的所有应用
     * @param memberId
     * @param sort
     * @param appType
     * @param name
     * @param vipTypeId
     * @param pageSize
     * @param pageNo
     * @return
     */
    @GetMapping("/find/cloud")
    public RespResult findcloud(Long memberId, @RequestParam String sort, @RequestParam Integer appType, String name, Long vipTypeId, Integer status,
                                @RequestParam Integer pageSize, @RequestParam Integer pageNo) {
        try {
            List<Long> cloudIds = new ArrayList<>();
            if (null != vipTypeId) {
                cloudIds = appVipTypeService.lambdaQuery()
                        .eq(AppVipType::getVipTypeId, vipTypeId)
                        .list().stream().map(AppVipType::getCloudId).collect(Collectors.toList());
            }
            LambdaQueryWrapper<AppCloud> appCloudLambdaQueryWrapper = new LambdaQueryWrapper<>();
            appCloudLambdaQueryWrapper
                    .eq(AppCloud::getAppType, appType)
                    .ne(AppCloud::getStatus, 1);
            if (appType == 1) {
                appCloudLambdaQueryWrapper.eq(AppCloud::getMemberId, memberId);
            }
            if (null != status) {
                appCloudLambdaQueryWrapper.eq(AppCloud::getPublishStatus, status);
            }
            if (org.springframework.util.StringUtils.hasText(name)) {
                appCloudLambdaQueryWrapper.like(AppCloud::getAppName, name);
            }
            if (!cloudIds.isEmpty()) {
                appCloudLambdaQueryWrapper.in(AppCloud::getId, cloudIds);
            }
            if (org.springframework.util.StringUtils.hasText(sort) && sort.toLowerCase(Locale.ROOT).equals("desc")) {
                appCloudLambdaQueryWrapper.orderByDesc(AppCloud::getSort);
            } else {
                appCloudLambdaQueryWrapper.orderByAsc(AppCloud::getSort);
            }
            Page<AppCloud> page = appCloudService.page(new Page<>(pageNo, pageSize), appCloudLambdaQueryWrapper);
            if (appType == 0) {
                for (AppCloud record : page.getRecords()) {
                    UacMember uacMember = uacMemberService.getById(record.getMemberId());
                    record.setMemberName(uacMember.getMemberName());
                    List<AppVipType> vipTypes = appVipTypeService.lambdaQuery().eq(AppVipType::getCloudId, record.getId()).list();
                    if (!vipTypes.isEmpty()) {
                        record.setVipTypeId(vipTypes);
                    }
                    List<AppCategory> serviceAppList = appCategoryService.findAppList(record.getId());
                    if (!serviceAppList.isEmpty()) {
                        record.setAppCategory(serviceAppList);
                    }
                }
            }
            PageList<AppCloud> list = new PageList<>();
            list.setTotalSize(page.getTotal());
            list.setRecords(page.getRecords());
            list.setSize(page.getSize());
            list.setCurrent(page.getCurrent());
            return handleResultList(list);
        } catch (Exception e) {
            return handleFail(e.getMessage());
        }
    }


    /**
     * 根据应用id修改应用状态
     * @param appId
     * @param status
     * @return
     */
    @GetMapping("/update/status")
    public RespResult<AppCloud> updateStatus(@RequestParam String appId, @RequestParam Integer status) {
        AppCloud appCloud = appCloudService.getById(appId);
        appCloud.setPublishStatus(status);
        appCloudService.updateById(appCloud);
        return handleResult(appCloud);
    }


    /**
     * 应用排序修改
     * @param appCateSort
     * @return
     */
    @PostMapping("sort/update")
    public RespResult appSortUpdate(@RequestBody List<AppCateSort> appCateSort) {
        if (CommonUtil.isEmpty(appCateSort)) {
            throw new AeotradeException("参数不能为空");
        }
        try {
            appCateSort.forEach(app -> {
                AppCloud appCloud = new AppCloud();
                appCloud.setId(app.getId());
                appCloud.setSort(app.getSort());
                appCloud.setRevision(0);
                appCloudService.updateById(appCloud);
            });
            return handleOK();
        } catch (Exception e) {
            e.printStackTrace();
            return handleFail("修改异常");
        }
    }

    //    @ApiOperation(httpMethod = "GET", value = "查询所有应用")
    @GetMapping("/list/all")
    public RespResult findList() {
        try {
            List<AppCloud> list = appCloudService.lambdaQuery()
                    .eq(AppCloud::getPublishStatus, 1)
                    .eq(AppCloud::getAppType, 0).list();
            return handleResult(list);
        } catch (Exception e) {
            return handleFail(e);
        }
    }



}

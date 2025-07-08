package com.aeotrade.provider.mamber.controller;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.service.*;
import com.aeotrade.provider.mamber.vo.AppCateSort;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2025/2/19 14:04
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class AppCategoryController extends BaseController {

    @Autowired
    private AppCategoryService appCategoryService;
    @Autowired
    private AppCategoryCloudService appCategoryCloudService;
    @Autowired
    private AppCloudService appCloudServicel;
    @Autowired
    private AppVipTypeService appVipTypeService;
    @Autowired
    private UacMemberService uacMemberService;

    /**
     * 应用类目添加
     *
     * @param appCategory
     * @return
     */
    @PostMapping("/save")
    public RespResult save(@RequestBody AppCategory appCategory) {
        try {
            if (null == appCategory) {
                throw new AeotradeException("参数不能为空");
            }
            appCategory.setCreatedTime(LocalDateTime.now());
            appCategory.setUpdatedTime(LocalDateTime.now());
            appCategory.setStatus(0);
            appCategory.setTypeStatus(0);
            if (appCategory.getSort() != null) {
                LambdaQueryWrapper<AppCategory> appCategoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                appCategoryLambdaQueryWrapper.eq(AppCategory::getSort, appCategory.getSort());
                Boolean count = appCategoryService.exists(appCategoryLambdaQueryWrapper);
                if (count) {
                    throw new AeotradeException("排序重复");
                }
            }
            appCategoryService.save(appCategory);
            return handleOK();
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof AeotradeException) {
                return handleFail(e.getMessage());
            } else {
                return handleFail("添加异常");
            }
        }
    }


    /**
     * 应用类目修改
     *
     * @param appCategory
     * @return
     */
    @PostMapping("/update")
    public RespResult update(@RequestBody AppCategory appCategory) {

        try {
            if (null == appCategory || appCategory.getCid() == null) {
                throw new AeotradeException("参数不能为空");
            }
            appCategory.setUpdatedTime(LocalDateTime.now());
            if (appCategory.getSort() != null) {
                LambdaQueryWrapper<AppCategory> appCategoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                appCategoryLambdaQueryWrapper.eq(AppCategory::getSort, appCategory.getSort());
                Boolean count = appCategoryService.exists(appCategoryLambdaQueryWrapper);
                if (count) {
                    throw new AeotradeException("排序重复");
                }
            }
            appCategoryService.updateById(appCategory);
            return handleOK();
        } catch (Exception e) {
            return handleFail(e.getMessage());
        }
    }

    /**
     * 根据类目id删除类目
     *
     * @param id
     * @return
     */
    @GetMapping("/delete/byid")
    public RespResult delete(@RequestParam Long id) {
        List<AppCategoryCloud> appCategoryClouds = appCategoryCloudService.lambdaQuery().eq(AppCategoryCloud::getCategoryId, id).list();
        if (appCategoryClouds.isEmpty()) {
            boolean removeById = appCategoryService.removeById(id);
            return handleResult(removeById);
        }
        return handleFail("该类目下有关联应用不能被删除");
    }

    /**
     * 根据类目id获取类型详情
     *
     * @param id
     * @return
     */
    @GetMapping("/find/byid")
    public RespResult findByid(@RequestParam Long id) {
        AppCategory appcategory = appCategoryService.getById(id);
        return handleResult(appcategory);
    }


    /**
     * 应用类目排序修改
     *
     * @param appCateSort
     * @return
     */
    @PostMapping("/sort/update")
    public RespResult sortUpdate(@RequestBody List<AppCateSort> appCateSort) {
        if (CommonUtil.isEmpty(appCateSort)) {
            throw new AeotradeException("参数不能为空");
        }
        try {
            appCateSort.forEach(app -> {
                AppCategory appCategory = appCategoryService.getById(app.getId());
                appCategory.setSort(app.getSort());
                appCategoryService.updateById(appCategory);
            });
            return handleOK();
        } catch (Exception e) {
            e.printStackTrace();
            return handleFail("修改异常");

        }
    }


    /**
     * 根据会员类型id集合获取该类型下所有类目
     *
     * @param vipTypeId
     * @return
     */
    @PostMapping("/find/viptypeid")
    public RespResult findviptypeid(@RequestBody List<String> vipTypeId) {
        Map<String, List<AppCategory>> map = new HashMap<>();
        for (String aLong : vipTypeId) {
            List<AppCategory> appCategories = appCategoryService.lambdaQuery().eq(AppCategory::getVipTypeId, aLong).list();
            map.put(String.valueOf(aLong), appCategories);
        }
        return handleResult(map);
    }


    /**
     * 编辑应用上架信息
     *
     * @param category
     * @param cloudId
     * @return
     */
    @GetMapping("/update/app")
    public RespResult updateApp(@RequestParam String[] category, @RequestParam String cloudId) {
        List<AppCategoryCloud> appCategoryClouds = appCategoryCloudService.lambdaQuery().eq(AppCategoryCloud::getCloudId, cloudId).list();
        appCategoryCloudService.removeBatchByIds(appCategoryClouds);
        for (String aLong : category) {
            AppCategoryCloud appCategoryCloud = new AppCategoryCloud();
            appCategoryCloud.setCategoryId(Long.valueOf(aLong));
            appCategoryCloud.setCloudId(Long.valueOf(cloudId));
            appCategoryCloudService.save(appCategoryCloud);
        }
        return handleOK();
    }


    /**
     * 应用分类和应用中心页面根据会员类型id查询其下所有类目及其应用
     *
     * @param vipTypeId
     * @return
     */
    @GetMapping("/find/vip/all")
    public RespResult findvipAll(@RequestParam String vipTypeId, Integer status) {
        List<AppCategory> appCategories = appCategoryService.lambdaQuery()
                .eq(AppCategory::getVipTypeId, vipTypeId)
                .orderByAsc(AppCategory::getSort)
                .list();
        for (AppCategory appCategory : appCategories) {
            List<AppCloud> appClouds = new ArrayList<>();
            List<AppCategoryCloud> categoryClouds = appCategoryCloudService.lambdaQuery()
                    .eq(AppCategoryCloud::getCategoryId, appCategory.getCid()).list();
            if (!categoryClouds.isEmpty()) {
                List<Long> cloudIds = categoryClouds.stream()
                        .filter(Objects::nonNull) // 过滤掉可能的null元素
                        .map(AppCategoryCloud::getCloudId)
                        .filter(Objects::nonNull) // 过滤掉可能的null云ID
                        .collect(Collectors.toList());
                if (!cloudIds.isEmpty()) {
                    LambdaQueryChainWrapper<AppCloud> queryWrapper = appCloudServicel.lambdaQuery();
                    // 判断 status 是否为空，如果不为空则添加条件
                    if (null != status) {
                        queryWrapper.eq(AppCloud::getPublishStatus, status);
                    }
                    // 处理 categoryClouds 集合
                    queryWrapper.in(AppCloud::getId, cloudIds);
                    // 添加排序条件
                    queryWrapper.orderByAsc(AppCloud::getSort);
                    // 执行查询
                    List<AppCloud> list = queryWrapper.list();
                    for (AppCloud appCloud : list) {
                        UacMember uacMember = uacMemberService.getById(appCloud.getMemberId());
                        appCloud.setMemberName(uacMember.getMemberName());
                        List<AppVipType> vipTypes = appVipTypeService.lambdaQuery().eq(AppVipType::getCloudId, appCloud.getId()).list();
                        appCloud.setVipTypeId(vipTypes);
                        List<AppCategory> serviceAppList = appCategoryService.findAppList(appCloud.getId());
                        if (!serviceAppList.isEmpty()) {
                            appCloud.setAppCategory(serviceAppList);
                        }
                    }
                    appClouds.addAll(list);
                }
            }
            appCategory.setAppCloudList(appClouds);
        }
        return handleResult(appCategories);
    }


}

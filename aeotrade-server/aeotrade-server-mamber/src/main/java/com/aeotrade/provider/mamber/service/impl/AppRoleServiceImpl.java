package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.mapper.AppRoleMapper;
import com.aeotrade.provider.mamber.service.*;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2025/3/19 11:23
 */
@Service
public class AppRoleServiceImpl extends MPJBaseServiceImpl<AppRoleMapper, AppRole> implements AppRoleService {
    @Autowired
    private AppCategoryService appCategoryService;
    @Autowired
    private AppCloudService appCloudService;
    @Autowired
    private AppCategoryCloudService appCategoryCloudService;
    @Autowired
    private AppVipTypeService appVipTypeService;

    @Override
    public List<AppRole> appManage(String memberId, String vipTypeId, String categroyId, String appName) {
        saveAppRole(memberId, vipTypeId);
        List<AppRole> list = new ArrayList<>();

        LambdaQueryChainWrapper<AppCategory> queryWrapper = appCategoryService.lambdaQuery();
        if (StringUtils.isNotEmpty(categroyId)) {
            queryWrapper.eq(AppCategory::getCid, categroyId);
        }
        queryWrapper.eq(AppCategory::getVipTypeId, vipTypeId);

        for (AppCategory appCategory : queryWrapper.list()) {
            List<Long> longs;
            if (StringUtils.isNotEmpty(appName)) {
                MPJLambdaWrapper<AppCategoryCloud> appCategoryCloudMPJLambdaWrapper = new MPJLambdaWrapper<>(AppCategoryCloud.class)
                        .disableSubLogicDel()
                        .disableLogicDel()
                        .selectAll(AppCategoryCloud.class)
                        .leftJoin(AppCloud.class, AppCloud::getId, AppCategoryCloud::getCloudId)
                        .eq(AppCategoryCloud::getCategoryId, appCategory.getCid())
                        .like(AppCloud::getAppName, appName);
                longs = appCategoryCloudMPJLambdaWrapper.list().stream()
                        .map(AppCategoryCloud::getCloudId)
                        .collect(Collectors.toList());
            } else {
                longs = appCategoryCloudService.lambdaQuery()
                        .eq(AppCategoryCloud::getCategoryId, appCategory.getCid()).list().stream()
                        .map(AppCategoryCloud::getCloudId)
                        .collect(Collectors.toList());
            }
            if (!longs.isEmpty()) {
                LambdaQueryChainWrapper<AppRole> appRolequeryWrapper = this.lambdaQuery();
                appRolequeryWrapper.in(AppRole::getAppId, longs);
                List<AppRole> roles = appRolequeryWrapper.list();
                List<AppRole> appRoleList = roles.stream()
                        .filter(appRole -> {
                            AppCloud appCloud = appCloudService.getById(appRole.getAppId());
                            if (null == appCloud) {
                                return false; // 返回 false 表示过滤掉该元素
                            } else {
                                String appCloudName = appCloud.getAppName();
                                if (StringUtils.isNotEmpty(appCloudName)) {
                                    appRole.setAppName(appCloudName);
                                }
                                String appLogo = appCloud.getAppLogo();
                                if (StringUtils.isNotEmpty(appLogo)) {
                                    appRole.setAppLogo(appLogo);
                                }
                                String appTypeName = appCategory.getAppTypeName();
                                if (StringUtils.isNotEmpty(appTypeName)) {
                                    appRole.setAppTypeName(appTypeName);
                                }
                                return true; // 返回 true 表示保留该元素
                            }
                        })
                        .collect(Collectors.toList());
                list.addAll(appRoleList);
            }
        }
        return list;
    }

    @Override
    public List<AppCategory> findvipAll(String vipTypeId, String memberId, String roles) {
        saveAppRole(memberId, vipTypeId);
        List<Long> appRoleOnes = this.lambdaQuery().eq(AppRole::getMemberId, memberId)
                .eq(AppRole::getVisibleRange, 1).list().stream()
                .map(AppRole::getAppId)
                .collect(Collectors.toList());
        // 将传入的 roles 字符串按逗号分割成数组
        String[] roleArray = roles.split(",");
        // 构建 SQL 片段，使用 FIND_IN_SET 函数来检查 userRole 字段中是否包含指定的角色
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < roleArray.length; i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("FIND_IN_SET('").append(roleArray[i]).append("', user_role) > 0");
        }
        List<Long> appRoleTwos = this.lambdaQuery().eq(AppRole::getMemberId, memberId)
                .eq(AppRole::getVisibleRange, 2)
                .apply(sql.toString()).list().stream()
                .map(AppRole::getAppId)
                .collect(Collectors.toList());
        if (!appRoleTwos.isEmpty()) {
            appRoleOnes.addAll(appRoleTwos);
        }
        List<AppCategory> appCategories = new ArrayList<>();
        appCategories.add(findziJian(memberId, appRoleOnes));
        List<AppCategory> categories = appCategoryService.lambdaQuery()
                .eq(AppCategory::getVipTypeId, vipTypeId)
                .orderByAsc(AppCategory::getSort)
                .list();
        for (AppCategory appCategory : categories) {
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
                    LambdaQueryChainWrapper<AppCloud> queryWrapper = appCloudService.lambdaQuery();
                    // 判断 status 是否为空，如果不为空则添加条件
                    queryWrapper.eq(AppCloud::getPublishStatus, 1);
                    queryWrapper.eq(AppCloud::getAppType, 0);
                    // 处理 categoryClouds 集合
                    queryWrapper.in(AppCloud::getId, cloudIds);
                    // 添加排序条件
                    queryWrapper.orderByAsc(AppCloud::getSort);
                    // 执行查询
                    List<AppCloud> list = queryWrapper.list().stream()
                            .filter(appCloud -> appRoleOnes.contains(appCloud.getId()))
                            .collect(Collectors.toList());
                    appClouds.addAll(list);
                }
            }
            appCategory.setAppCloudList(appClouds);
        }
        appCategories.addAll(categories);

        return appCategories;
    }

    @Override
    public List<AppRole> appManageZiJian(String memberId, String vipTypeId, String appName) {
        saveAppRole(memberId, vipTypeId);
        List<AppRole> list = new ArrayList<>();
        LambdaQueryChainWrapper<AppCloud> queryWrapper = appCloudService.lambdaQuery();
        queryWrapper.eq(AppCloud::getPublishStatus, 1);
        queryWrapper.eq(AppCloud::getMemberId, memberId);
        queryWrapper.eq(AppCloud::getAppType, 1);
        if (StringUtils.isNotEmpty(appName)) {
            queryWrapper.like(AppCloud::getAppName, appName);
        }

        List<AppCloud> zijianApp = queryWrapper.list();

        if (!zijianApp.isEmpty()) {
            List<Long> longList = zijianApp.stream().map(AppCloud::getId).collect(Collectors.toList());
            LambdaQueryChainWrapper<AppRole> appRolequeryWrapper = this.lambdaQuery();
            appRolequeryWrapper.in(AppRole::getAppId, longList);
            List<AppRole> roles = appRolequeryWrapper.list().stream()
                    .filter(appRole -> {
                        AppCloud appCloud = appCloudService.getById(appRole.getAppId());
                        if (null == appCloud) {
                            return false; // 返回 false 表示过滤掉该元素
                        } else {
                            String appCloudName = appCloud.getAppName();
                            if (StringUtils.isNotEmpty(appCloudName)) {
                                appRole.setAppName(appCloudName);
                            }
                            String appLogo = appCloud.getAppLogo();
                            if (StringUtils.isNotEmpty(appLogo)) {
                                appRole.setAppLogo(appLogo);
                            }
                            appRole.setAppTypeName("自建应用");
                            return true; // 返回 true 表示保留该元素
                        }
                    })
                    .collect(Collectors.toList());
            list.addAll(roles);
        }
        return list;
    }

    private AppCategory findziJian(String memberId, List<Long> appRoleOnes) {
        AppCategory appCategory = new AppCategory();
        appCategory.setAppTypeName("自建应用");
        appCategory.setImg("SETTLEMENT_PAYMENT");
        appCategory.setSort(0);
        appCategory.setCid(10000L);
        LambdaQueryChainWrapper<AppCloud> queryWrapper = appCloudService.lambdaQuery();
        // 判断 status 是否为空，如果不为空则添加条件
        queryWrapper.eq(AppCloud::getPublishStatus, 1);
        queryWrapper.eq(AppCloud::getMemberId, memberId);
        queryWrapper.eq(AppCloud::getAppType, 1);
        queryWrapper.orderByAsc(AppCloud::getSort);
        List<AppCloud> list = queryWrapper.list();
        if (!appRoleOnes.isEmpty()) {
            list.stream()
                    .filter(appCloud -> appRoleOnes.contains(appCloud.getId()))
                    .collect(Collectors.toList());
        }
        if (!list.isEmpty()) {
            appCategory.setAppCloudList(list);
        }
        return appCategory;
    }


    private void saveAppRole(String memberId, String vipTypeId) {
        //查询该企业所有应用权限
        List<AppRole> appRoles = this.lambdaQuery().eq(AppRole::getMemberId, memberId).list();
        //查询该企业所有公开应用
        List<Long> collected = appVipTypeService.lambdaQuery().eq(AppVipType::getVipTypeId, vipTypeId).list().stream()
                .map(AppVipType::getCloudId)
                .collect(Collectors.toList());
        List<AppCloud> appClouds = new ArrayList<>();
        if (!collected.isEmpty()) {
            List<AppCloud> list = appCloudService.lambdaQuery().in(AppCloud::getId, collected)
                    .eq(AppCloud::getAppType, 0).eq(AppCloud::getPublishStatus, 1).list();
            if (!list.isEmpty()) {
                appClouds.addAll(list);
            }
        }
        //查询该企业所有自建应用
        List<AppCloud> appCloudList = appCloudService.lambdaQuery().eq(AppCloud::getAppType, 1)
                .eq(AppCloud::getPublishStatus, 1).eq(AppCloud::getMemberId, memberId).list();

        if (!appCloudList.isEmpty()) {
            // 合并公开应用和自建应用两个列表
            appClouds.addAll(appCloudList);
        }
        if (!appClouds.isEmpty()) {
            // 提取 AppCloud 的 id 列表
            List<Long> cloudIds = appClouds.stream()
                    .map(AppCloud::getId)
                    .collect(Collectors.toList());

            // 提取 AppRole 的 appId 列表
            List<Long> roleCloudIds = appRoles.stream()
                    .map(AppRole::getAppId)
                    .collect(Collectors.toList());

            // 处理添加AppRole操作
            cloudIds.stream()
                    .filter(cloudId -> !roleCloudIds.contains(cloudId))
                    .map(cloudId -> {
                        AppRole newAppRole = new AppRole();
                        newAppRole.setAppId(cloudId);
                        newAppRole.setMemberId(Long.valueOf(memberId));
                        newAppRole.setVisibleRange(1);
                        return newAppRole;
                    })
                    .forEach(this::save);

            // 处理AppRole删除操作
            appRoles.stream()
                    .filter(appRole -> !cloudIds.contains(appRole.getAppId()))
                    .map(AppRole::getId)
                    .forEach(this::removeById);
        }
    }

}

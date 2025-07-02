package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.AppCategory;
import com.aeotrade.provider.mamber.entity.AppCategoryCloud;
import com.aeotrade.provider.mamber.mapper.AppCategoryMapper;
import com.aeotrade.provider.mamber.service.AppCategoryService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 应用分类表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
@Service
public class AppCategoryServiceImpl extends MPJBaseServiceImpl<AppCategoryMapper, AppCategory> implements AppCategoryService {


    @Override
    public List<AppCategory> findAppList(Long id) {
        return this.selectJoinList(AppCategory.class,
                MPJWrappers.<AppCategory>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(AppCategory.class)
                        .leftJoin(AppCategoryCloud.class, AppCategoryCloud::getCategoryId, AppCategory::getCid)
                        .eq(AppCategoryCloud::getCloudId, id));
    }
}

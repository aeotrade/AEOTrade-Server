package com.aeotrade.provider.mamber.service;


import com.aeotrade.provider.mamber.entity.AppCategory;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;


/**
 * <p>
 * 应用分类表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
public interface AppCategoryService extends MPJBaseService<AppCategory> {

    List<AppCategory> findAppList(Long id);
}

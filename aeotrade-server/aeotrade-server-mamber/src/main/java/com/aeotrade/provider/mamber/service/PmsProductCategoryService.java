package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.PmsProductCategory;
import com.aeotrade.provider.mamber.vo.PmsProductCategoryParam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 14:49
 */
public interface PmsProductCategoryService extends IService<PmsProductCategory> {
    int create(PmsProductCategoryParam productCategoryParam);

    int updateNavStatus(List<Long> ids, Integer navStatus);

    int updateShowStatus(List<Long> ids, Integer showStatus);
}

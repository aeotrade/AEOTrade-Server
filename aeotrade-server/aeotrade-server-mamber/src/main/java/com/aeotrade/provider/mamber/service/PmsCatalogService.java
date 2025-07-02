package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.PmsCatalog;
import com.aeotrade.provider.mamber.vo.PmsCatalogInfo;
import com.aeotrade.provider.mamber.vo.PmsCatalogInfoDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 16:06
 */
public interface PmsCatalogService extends IService<PmsCatalog> {
    Integer delRedis(Long id);

    long del(Long id, Integer integer);

    long dels(Long id);

    List<PmsCatalogInfo> appendDetail(List<PmsCatalogInfo> catalogList);

    void appendDetailId(List<PmsCatalogInfo> catalogList);
}

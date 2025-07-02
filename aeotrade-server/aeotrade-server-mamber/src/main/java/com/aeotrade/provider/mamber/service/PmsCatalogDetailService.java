package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.PmsCatalogDetail;
import com.aeotrade.suppot.RespResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 15:52
 */
public interface PmsCatalogDetailService extends IService<PmsCatalogDetail> {
    RespResult listCatalogDetails(long catalogId, Page page);

    RespResult pageListCatalogDetails(long catalogId, Integer pageSize, Integer pageNum);
}

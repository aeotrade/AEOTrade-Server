package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.AppCloud;
import com.aeotrade.provider.mamber.mapper.AppCloudMapper;
import com.aeotrade.provider.mamber.service.AppCloudService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 应用表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
@Service
public class AppCloudServiceImpl extends MPJBaseServiceImpl<AppCloudMapper, AppCloud> implements AppCloudService {
    @Override
    public List<Document> findAppList(String substring, String substring1, String id, int i, String appCloud, long size, long current) {
        List<Document> documentList = new ArrayList<>();
        LambdaQueryWrapper<AppCloud> appCloudLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appCloudLambdaQueryWrapper.in(AppCloud::getId, id);
        appCloudLambdaQueryWrapper.orderByDesc(AppCloud::getId);
        List<AppCloud> records = this.page(new Page<>(size, current), appCloudLambdaQueryWrapper).getRecords();
        for (AppCloud record : records) {
            Document document = new Document();
            document.put("id", record.getId());
            document.put("name", record.getAppName());
            document.put("pic", record.getAppLogo());
            document.put("url", record.getUrl());
            document.put("subTitle", record.getSubhead());
            document.put("createdTime", record.getCreatedTime());
            documentList.add(document);
        }
        return documentList;
    }
}

package com.aeotrade.provider.mamber.service;


import com.aeotrade.provider.mamber.entity.AppCloud;
import com.github.yulichang.base.MPJBaseService;
import org.bson.Document;

import java.util.List;

/**
 * <p>
 * 应用表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
public interface AppCloudService extends MPJBaseService<AppCloud> {

    List<Document> findAppList(String substring, String substring1, String id, int i, String appCloud, long size, long current);
}

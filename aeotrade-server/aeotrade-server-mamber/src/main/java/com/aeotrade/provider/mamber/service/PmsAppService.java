package com.aeotrade.provider.mamber.service;


import com.aeotrade.provider.mamber.entity.PmsApp;
import com.aeotrade.suppot.PageList;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.bson.Document;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 11:23
 */
public interface PmsAppService extends IService<PmsApp> {
    PageList<PmsApp> findList(Page page, String appName);

    PageList<PmsApp> findAppListBymemberId(Page page, Long memberId);

    void updateAppMemberList(Long appId, Long memberId);

    List<Document> findDynamicConditionist(String s, String pms_app, String sort, String s1);

    List<Document> findDynamicConditionistPage(String s, String pms_app, String sort, String substring, long current, long size);
}

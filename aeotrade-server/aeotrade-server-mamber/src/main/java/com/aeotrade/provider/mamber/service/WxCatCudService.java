package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.WxCatCud;
import com.aeotrade.provider.mamber.entity.WxUcd;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.bson.Document;

import java.util.List;

/**
 * 图文标签关系表
 * @Author: yewei
 * @Date: 2020/4/1 11:13
 */
public interface WxCatCudService extends IService<WxCatCud> {

    Boolean insertWxCatCud(WxCatCud wxCatCud);

    Boolean deleteWxCatCudByCatId(Long catId);

    Boolean deleteWxCatCudByUcdId(String ucdId);

    List<WxCatCud> findListByCatId(Long catId);

    List<WxCatCud> findListByCududcId(String ucdId);


    List<Document> findAIMember(String fieldNames, String id, String sort, Integer type, String tableName, Integer pageNum, Integer pageSize);

    Page<WxUcd> findPageAIMember(String fieldNames, String id, String sort, Integer type, String tableName, Integer pageNum, Integer pageSize);
}

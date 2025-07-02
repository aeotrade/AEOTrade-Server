package com.aeotrade.provider.mamber.service;

import cn.hutool.json.JSONObject;
import com.aeotrade.provider.mamber.entity.WxUcd;
import com.aeotrade.provider.mamber.vo.WxUcdVo;
import com.baomidou.mybatisplus.extension.service.IService;


import java.util.List;

/**
 * 微信菜单
 * @Author: yewei
 * @Date: 2020/3/30 15:24
 */
public interface WxUcdService extends IService<WxUcd> {
        /**根据mediaId和图文类型查询*/
        List<WxUcd> findWxUcdById(String mediaId,String type);

        List<WxUcd> findWxUcdOneById(String mediaId);
        /**插入一条数据*/
        int insertWxUcd(WxUcd wxUcd);

        int updateByMediaId(WxUcd wxUcd);
        /**删除*/
        int deleteByMediaId(String mediaId);

        void insertCover(JSONObject data);

        void deleteById(Long id);

        void updatesById(JSONObject data);

        WxUcdVo findById(Long id);

          String tihuan();
}

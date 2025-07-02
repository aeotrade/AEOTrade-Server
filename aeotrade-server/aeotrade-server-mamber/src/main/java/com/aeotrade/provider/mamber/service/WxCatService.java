package com.aeotrade.provider.mamber.service;



import com.aeotrade.provider.mamber.entity.WxCat;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/3/30 19:43
 */
public interface WxCatService extends IService<WxCat> {
    List<WxCat> findWxCatList();


    int insertWxCat(WxCat wxCat);

    int udpateWxCat(WxCat wxCat);
}

package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.WxCat;
import com.aeotrade.provider.mamber.mapper.WxCatMapper;
import com.aeotrade.provider.mamber.service.WxCatService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/3/30 19:44
 */
@Service
@DS("weixin")
public class WxCatServiceImpl extends ServiceImpl<WxCatMapper, WxCat>  implements WxCatService {

    @Autowired
    private WxCatMapper wxCatMapper;

    @Override
    public List<WxCat> findWxCatList() {
        QueryWrapper<WxCat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_parent",1);
        List<WxCat> wxCats = wxCatMapper.selectList(null);
        return wxCats;
    }



    @Override
    public int insertWxCat(WxCat wxCat) {
        return wxCatMapper.insert(wxCat);
    }

    @Override
    public int udpateWxCat(WxCat wxCat) {
        QueryWrapper<WxCat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",wxCat.getId());
        return wxCatMapper.update(wxCat,queryWrapper);
    }
}

package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.WxCatCud;
import com.aeotrade.provider.mamber.entity.WxUcd;
import com.aeotrade.provider.mamber.service.WxCatCudService;
import com.aeotrade.provider.mamber.service.WxUcdService;
import com.aeotrade.provider.mamber.mapper.WxCatCudMapper;
import com.aeotrade.provider.mamber.utils.WxDateUitls;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/4/1 11:14
 */
@Service
public class WxCatCudServiceImpl extends ServiceImpl<WxCatCudMapper, WxCatCud> implements WxCatCudService {

    @Autowired
    private WxCatCudService wxCatCudMapper;
    @Autowired
    private WxUcdService wxUcdMapper;

    @Override
    public Boolean insertWxCatCud(WxCatCud wxCatCud) {
        return wxCatCudMapper.save(wxCatCud);
    }

    @Override
    public Boolean deleteWxCatCudByCatId(Long catId) {
        QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cat_id",catId);
        return wxCatCudMapper.remove(queryWrapper);
    }

    @Override
    public Boolean deleteWxCatCudByUcdId(String ucdId) {
        QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ucd_id",ucdId);
        return wxCatCudMapper.remove(queryWrapper);
    }

    @Override
    public List<WxCatCud> findListByCatId(Long catId) {
        return wxCatCudMapper.lambdaQuery().eq(WxCatCud::getCatId,catId).list();
    }

    @Override
    public List<WxCatCud> findListByCududcId(String ucdId) {
        QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ucd_id",ucdId);
        return wxCatCudMapper.lambdaQuery().eq(WxCatCud::getUcdId,ucdId).list();
    }

    @Override
    public List<Document> findAIMember(String fieldNames, String id, String sort, Integer type, String tableName, Integer pageNum, Integer pageSize) {
        List<String> list= Arrays.asList( id.split(","));
        List<Document> vo = new ArrayList<>();
        for (String s : list) {
            WxUcd aiMember =  wxUcdMapper.getById(s);
            if(null!=aiMember){
                Document d= new Document();
                d.put("content_type",type);
                d.put("id",aiMember.getId());
                d.put("name",aiMember.getTitle());
                d.put("pic",aiMember.getTitleImage());
                d.put("description",aiMember.getDescription());
                d.put("createTime", WxDateUitls.getDateTimeStamp(aiMember.getCreatedTime()));
                d.put("author",aiMember.getAuthor());
                d.put("icon",aiMember.getIcon());
                d.put("iconHover",aiMember.getIconHover());
                vo.add(d);
            }
        }
        return vo;
    }

    @Override
    public Page<WxUcd> findPageAIMember(String fieldNames, String id, String sort, Integer type, String tableName, Integer pageNum, Integer pageSize) {
        String[] split = StringUtils.split(id, ",");
        //分页查询起始页
        int start = (pageNum-1)*pageSize;
        LambdaQueryWrapper<WxUcd> wxCatCudLambdaQueryWrapper = new LambdaQueryWrapper<>();
        wxCatCudLambdaQueryWrapper.in(WxUcd::getId,id);
        wxCatCudLambdaQueryWrapper.orderByDesc(WxUcd::getId);
        Page<WxUcd> wxUcdPage = wxUcdMapper.page(new Page<>(pageNum,pageSize), wxCatCudLambdaQueryWrapper);
        Page<WxUcd> page = new Page<>();
        page.setTotal(Long.valueOf(split.length));
        page.setSize(Long.valueOf(pageSize));
        page.setCurrent(Long.valueOf(pageNum));
        page.setRecords(wxUcdPage.getRecords());
        return page;
    }


}

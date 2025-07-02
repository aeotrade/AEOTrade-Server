package com.aeotrade.provider.mamber.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import com.aeotrade.provider.mamber.utils.ImageUtil;
import com.aeotrade.provider.mamber.entity.WxCat;
import com.aeotrade.provider.mamber.entity.WxCatCud;
import com.aeotrade.provider.mamber.entity.WxUcd;

import com.aeotrade.provider.mamber.mapper.WxCatCudMapper;
import com.aeotrade.provider.mamber.mapper.WxCatMapper;
import com.aeotrade.provider.mamber.mapper.WxUcdMapper;
import com.aeotrade.provider.mamber.service.WxUcdService;
import com.aeotrade.provider.mamber.vo.AticlesDto;
import com.aeotrade.provider.mamber.vo.Cid;
import com.aeotrade.provider.mamber.vo.WxUcdVo;
import com.aeotrade.provider.mamber.vo.cidDto;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @Author: yewei
 * @Date: 2020/3/30 15:25
 */
@Service
@DS("weixin")
public class WxUcdServiceImpl extends ServiceImpl<WxUcdMapper, WxUcd> implements WxUcdService {
    @Autowired
    private WxUcdMapper wxUcdMapper;
    @Autowired
    private WxCatCudMapper wxCatCudMapper;
    @Autowired
    private WxCatMapper wxCatMapper;


    @Override
    public List<WxUcd> findWxUcdById(String mediaId, String type) {

        QueryWrapper<WxUcd > queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("media_id",mediaId)
                .eq("type",type);

        return wxUcdMapper.selectList(queryWrapper);
    }

    @Override
    public List<WxUcd> findWxUcdOneById(String mediaId) {
        QueryWrapper<WxUcd > queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("media_id",mediaId);

        return wxUcdMapper.selectList(queryWrapper);
    }


    @Override
    public int insertWxUcd(WxUcd wxUcd) {
        return wxUcdMapper.insert(wxUcd);
    }

    @Override
    public int updateByMediaId(WxUcd wxUcd) {
        QueryWrapper<WxUcd> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("media_id",wxUcd.getMediaId());
        return wxUcdMapper.update(wxUcd,queryWrapper);
    }

    @Override
    public int deleteByMediaId(String mediaId) {
        QueryWrapper<WxUcd> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("media_id",mediaId);
        return wxUcdMapper.delete(queryWrapper);
    }

    @Override
    public void insertCover(JSONObject data) {
        String mediaId = UUID.randomUUID().toString().replace("", "");
        JSONArray jSONArray = data.getJSONArray("articles");
        //分类 long sortCid = Long.valueOf(String.valueOf(data.get("sortCid"))).longValue();
        String titleImage = String.valueOf(data.get("titleImage"));
        JSONArray cid = data.getJSONArray("cid");
        List<AticlesDto> aticlesDtos = jSONArray.toList(AticlesDto.class);
        if(aticlesDtos.size()>0){
            WxUcd wxUcd = null;
            for (AticlesDto a :aticlesDtos) {
                wxUcd = new WxUcd();
                if(null!=a){
                    BeanUtils.copyProperties(a,wxUcd );
                    wxUcd.setMediaId(mediaId);
                    // wxUcd.setUrl(url);
                    wxUcd.setTitleImage(titleImage);
                    wxUcd.setCreatedTime(new Timestamp(System.currentTimeMillis()));
                    wxUcd.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    wxUcd.setThumbMediaId(a.getThumb_media_id());
                    wxUcd.setContentSourceUrl(a.getContentSourceUrl());
                    wxUcd.setShowCoverPic(a.getShow_cover_pic());
                    wxUcd.setIcon(a.getIcon());
                    wxUcd.setIconHover(a.getIconHover());
                    //分类wxUcd.setCid(sortCid);
                    wxUcd.setType("1");
                    wxUcdMapper.insert(wxUcd);
                }
            }
        }
        if(cid!=null){
            List<cidDto> cidDtos = cid.toList(cidDto.class);
            WxCatCud wxCatCud = null;
            for (cidDto cids : cidDtos) {
                wxCatCud= new WxCatCud();
                wxCatCud.setCatId(cids.getId());
                wxCatCud.setUcdId(mediaId);
                wxCatCudMapper.insert(wxCatCud);
            }
        }

    }

    @Override
    public void deleteById(Long id) {
        wxUcdMapper.deleteById(id);
        QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cat_id",id);
        wxCatCudMapper.delete(queryWrapper);
    }

    @Override

    public void updatesById(JSONObject data) {
        JSONArray jSONArray = data.getJSONArray("articles");
        String mediaId = String.valueOf(data.get("mediaId"));
        long id = Long.valueOf(String.valueOf(data.get("id"))).longValue();
        List<AticlesDto> aticlesDtos = jSONArray.toList(AticlesDto.class);
        JSONArray cid = data.getJSONArray("cid");
        String titleImage = String.valueOf(data.get("titleImage"));
        if(aticlesDtos.size()>0){
            WxUcd wxUcd = null;
            for (AticlesDto a :aticlesDtos) {
                wxUcd = new WxUcd();
                if(null!=a){
                    BeanUtils.copyProperties(a,wxUcd );
                    wxUcd.setId(id);
                    wxUcd.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                    wxUcd.setThumbMediaId(a.getThumb_media_id());
                    wxUcd.setContentSourceUrl(a.getContentSourceUrl());
                    wxUcd.setShowCoverPic(a.getShow_cover_pic());
                    wxUcd.setType("1");
                    wxUcd.setTitleImage(titleImage);
                    wxUcd.setIcon(a.getIcon());
                    wxUcd.setIconHover(a.getIconHover());
                    wxUcdMapper.updateById(wxUcd );
                }
            }
            /**修改标签信息*/
            QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ucd_id",mediaId);
            wxCatCudMapper.delete(queryWrapper);
            WxUcd wxUcd1 = wxUcdMapper.selectById(id);
             if(cid!=null && wxUcd1!=null){
             List<cidDto> cidDtos = cid.toList(cidDto.class);
             WxCatCud wxCatCud = null;
             for (cidDto cids : cidDtos) {

             wxCatCud= new WxCatCud();
             wxCatCud.setCatId(cids.getId());
             wxCatCud.setUcdId(wxUcd1.getMediaId());
             wxCatCudMapper.insert(wxCatCud);
             }
             }
        }
    }

    @Override
    public WxUcdVo findById(Long id) {
        WxUcdVo vo = new WxUcdVo();
        WxUcd wxUcd = wxUcdMapper.selectById(id);
        if(wxUcd!=null) {
            QueryWrapper<WxCatCud> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("ucd_id", wxUcd.getMediaId());
            List<WxCatCud> wxCatCuds = wxCatCudMapper.selectList(queryWrapper);
            List<Cid> cidList = new ArrayList<>();
            Cid cid = null;
            for (WxCatCud cud : wxCatCuds) {
                WxCat wxCat = wxCatMapper.selectById(cud.getCatId());
                cid= new Cid();
                cid.setId(wxCat.getId());
                cidList.add(cid);
            }
            vo.setCid(cidList);
            vo.setWxUcd(wxUcd);
            return vo;
        }
        return vo;
    }

    @Override
    public String tihuan() {

        List<WxUcd> ucds=wxUcdMapper.selectList(new QueryWrapper<>());
        ucds.forEach(i->{
            if(StringUtils.isNotEmpty(i.getTitleImage())) {
                String s = ImageUtil.UrlDownload(i.getTitleImage());
                i.setTitleImage(s);
                wxUcdMapper.updateById(i);
            }
        });

        return "wxUcd.getTitleImage()";
    }

}



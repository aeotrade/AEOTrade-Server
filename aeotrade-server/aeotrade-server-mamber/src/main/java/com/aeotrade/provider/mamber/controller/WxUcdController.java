package com.aeotrade.provider.mamber.controller;

import cn.hutool.json.JSONObject;
import com.aeotrade.provider.mamber.common.AjaxResult;
import com.aeotrade.provider.mamber.utils.ToolUtils;
import com.aeotrade.provider.mamber.entity.WxCat;
import com.aeotrade.provider.mamber.entity.WxCatCud;
import com.aeotrade.provider.mamber.entity.WxUcd;
import com.aeotrade.provider.mamber.service.WxCatCudService;
import com.aeotrade.provider.mamber.service.WxCatService;
import com.aeotrade.provider.mamber.service.WxUcdService;
import com.aeotrade.provider.mamber.vo.PageLists;
import com.aeotrade.provider.mamber.vo.WxUcdTime;
import com.aeotrade.provider.mamber.vo.WxUcdVo;
import com.aeotrade.suppot.BaseController;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 微信菜单
 * @Author: yewei
 * @Date: 2020/3/30 16:04
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/wxcat")
@CrossOrigin(maxAge = 3600,origins = "*")
public class WxUcdController extends BaseController {
    @Autowired
    private WxUcdService wxUcdService;

    @Autowired
    private WxCatService wxCatService;

    @Autowired
    private WxCatCudService wxCatCudService;


    /**
     * 文章新增
     * @param data
     * @return
     */
    @PostMapping("/cover")
    public AjaxResult insertCover(@RequestBody JSONObject data){
        try {

            wxUcdService.insertCover(data);
            return AjaxResult.success("添加成功");
        }catch (Exception e){
            log.warn(e.getMessage());
            return AjaxResult.error("添加失败");
        }
    }
    @GetMapping("/cover")
    public AjaxResult findById(Long id ){
        WxUcdVo byId = wxUcdService.findById(id);
        return AjaxResult.success(Optional.ofNullable(byId).orElseGet(WxUcdVo::new));
    }

    @GetMapping("/dele")
    public AjaxResult deleteById(Long id ){
        try {
            wxUcdService.deleteById(id);
            return AjaxResult.success("删除成功");
        }catch (Exception e){
            return AjaxResult.error("删除失败");
        }
    }

    @PostMapping("/update/ucd")
    public AjaxResult updateById(@RequestBody JSONObject data){
        try {
            wxUcdService.updatesById(data);
            return AjaxResult.success("修改成功");
        }catch (Exception e){
            return AjaxResult.error("修改失败");
        }
    }
    /**
     * 查询微信图文消息列表
     * @param
     * @return
     */
    @GetMapping("/list")
    public AjaxResult wxUcdList(long pageNum ,long pageSize,String title){
        QueryWrapper<WxUcd> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_time");
        if(StringUtils.isNotEmpty(title)){
            queryWrapper.like("title",title);
        }
        Page<WxUcd> page1 = wxUcdService.page( new Page<>(pageNum,pageSize), queryWrapper);
        PageLists<WxUcdTime> pageList = new PageLists<>();
        //将date转换为时间戳
        List<WxUcd> records = page1.getRecords();
        List<WxUcdTime> volist = new ArrayList<>();
        records.forEach(i->{
            WxUcdTime ucdTime = new WxUcdTime();
            BeanUtils.copyProperties( i,ucdTime);
            ucdTime.setCreateTime(ToolUtils.getDateTimeStamp(i.getCreatedTime()));
            ucdTime.setUpdateTime(ToolUtils.getDateTimeStamp(i.getUpdateTime()));
            volist.add(ucdTime);
        });

        pageList.setData(volist);
        pageList.setPageNum(page1.getCurrent());
        pageList.setPageSize(page1.getSize());
        pageList.setTotalPage(page1.getTotal()/page1.getSize());
        pageList.setTotal(page1.getTotal());
        return AjaxResult.success(pageList);
    }


    /**
     * 微信图文消息详情
     * @param mediaId
     * @return
     */
    @GetMapping("/ucd")
    public AjaxResult findWxUcd(String mediaId){
        List<WxUcd> wxUcdById = wxUcdService.findWxUcdById(mediaId, "1");

        return AjaxResult.success(wxUcdById );
    }

    @GetMapping("/delete")
    public AjaxResult deleteWxUcd(String mediaId){
        try {
            wxUcdService.deleteByMediaId(mediaId);
            return AjaxResult.success("删除成功");
        }catch (Exception e){
            return  AjaxResult.error("删除错误");
        }
    }
    /**
     * 获取图文标签
     * @return
     */
    @GetMapping("/cid")
    public AjaxResult findCat(){
        List<WxCat> wxCats =  wxCatService.findWxCatList();
        return  AjaxResult.success(wxCats);
    }

    /**
     * 添加分类标签
     */
    @PostMapping("/save")
    public AjaxResult insertCat(@RequestBody WxCat wxCat){
        if(null!=wxCat){
            int rows =wxCatService.insertWxCat(wxCat);
            if(rows>0){
                return AjaxResult.success("添加成功");
            }else {
                return AjaxResult.error("添加失败");
            }
        }
        return   null;
    }

    /**
     * 修改分类标签
     * @param wxCat
     * @return
     */
    @PostMapping("/update")
    public AjaxResult updateWxCat(@RequestBody WxCat wxCat){
        if(null!=wxCat && wxCat.getId()!=null ){
            int rows =wxCatService.udpateWxCat(wxCat);
            if(rows>0){
                return AjaxResult.success("修改成功");
            }else {
                return AjaxResult.error("修改失败");
            }
        }
        return   null;
    }

    /**
     * 标签修改详情
     * @param mediaId
     * @return
     */
    @GetMapping("/cat")
    public AjaxResult findUpdateWxCat( String mediaId){
        List<WxCatCud> listByCududcId = wxCatCudService.findListByCududcId(mediaId);
        return AjaxResult.success(listByCududcId);
    }
}

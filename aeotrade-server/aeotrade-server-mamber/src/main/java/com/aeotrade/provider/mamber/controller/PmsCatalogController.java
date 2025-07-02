package com.aeotrade.provider.mamber.controller;

import com.aeotrade.provider.mamber.entity.PmsCatalog;
import com.aeotrade.provider.mamber.entity.PmsCatalogDetail;
import com.aeotrade.provider.mamber.service.PmsCatalogDetailService;
import com.aeotrade.provider.mamber.service.PmsCatalogService;
import com.aeotrade.provider.mamber.vo.PmsCatalogInfo;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
//@Api(tags = "PmsCatalogController", description = "频道栏目管理")
@RequestMapping("/catalog")
@Slf4j
public class PmsCatalogController extends BaseController {
    @Autowired
    private PmsCatalogDetailService catalogDetailService;
    @Autowired
    private PmsCatalogService catalogService;

    //@ApiOperation("创建栏目的详情")
    @RequestMapping(value = "create/detail", method = RequestMethod.POST)
    @ResponseBody
    public RespResult createDetail(@RequestBody PmsCatalogDetail catalogDetail) {
        Boolean catalogId = catalogDetailService.saveOrUpdate(catalogDetail);
        return handleResult(catalogDetail);
    }



    //@ApiOperation("列出指定分类栏目的详情")
    @RequestMapping(value = "list/details", method = RequestMethod.GET)
    @ResponseBody
    public RespResult listCatalogDetails(long catalogId, @RequestParam(value = "pageSize", defaultValue = "1000") Integer pageSize,
                                         @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) throws Exception {
        try {
            Page page = null;
            if(pageSize!=null){
                page= new Page(pageNum,pageSize);
            }
            return catalogDetailService.listCatalogDetails(catalogId,page);
        }catch (Exception e){
            log.warn(e.getMessage());
            return RespResultMapper.wrap(200,"数据异常",null);
        }
    }
    //@ApiOperation("分页获取咨询")
    @RequestMapping(value = "page/list/details", method = RequestMethod.GET)
    @ResponseBody
    public RespResult pageListCatalogDetails(long catalogId,
                                             @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                                             @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) throws Exception {
        try {
            return catalogDetailService.pageListCatalogDetails(catalogId,pageSize,pageNum);
        }catch (Exception e){
            log.warn(e.getMessage());
            return RespResultMapper.wrap(200,"数据异常",null);
        }
    }

    //@ApiOperation("创建栏目分类")
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public RespResult create(@RequestBody PmsCatalog catalog) {
        catalog.setId(null);
        Boolean catalogId = catalogService.save(catalog);
        return handleResult(catalog);
    }

    //@ApiOperation("删除栏目分类")
    @RequestMapping(value = "del", method = RequestMethod.POST)
    @ResponseBody
    public RespResult deleteCatalog(Long id) {
        Integer integer = catalogService.delRedis(id);
        if(integer!=null) {
            long count = catalogService.del(id, integer);
            System.out.println(id + " delete count = " + count);
        }else {
            long count = catalogService.dels(id);
            System.out.println(id + " delete count = " + count);
        }

        return handleOK();
    }

    //@ApiOperation("列出指定频道下的栏目分类列表")
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    public RespResult listCatalog(String channelSectionId, Boolean includeDetail) {
        if(StringUtils.isEmpty(channelSectionId) || channelSectionId.equals("NaN")){
            List<PmsCatalogInfo> collect=new ArrayList<>();
            return  handleResult(collect);
        }
        List<PmsCatalog> catalogList = catalogService.lambdaQuery()
                .eq(PmsCatalog::getChannelSectionId,Integer.valueOf(channelSectionId))
                .eq(PmsCatalog::getParentId,0)
                .orderByAsc(PmsCatalog::getSort)
                .list();
        List<PmsCatalogInfo> infoList = to(catalogList);
        infoList=loadChildren(infoList, null);
        // 如果包括详情
        if (includeDetail !=null && includeDetail) {
            infoList= catalogService.appendDetail(infoList);
        }
        //排序
        List<PmsCatalogInfo> vo = new ArrayList<>();
        infoList.forEach(i->{
            List<PmsCatalogInfo> collect = i.getChildren().stream().sorted(Comparator.comparing(PmsCatalogInfo::getSort)).collect(Collectors.toList());
            i.setChildren(collect);
            vo.add(i);
        });
        List<PmsCatalogInfo> collect = vo.stream().sorted(Comparator.comparing(PmsCatalogInfo::getSort)).collect(Collectors.toList());
        return  handleResult(collect);
    }

    //@ApiOperation("列出指定频道下的可以显示的栏目分类列表")
    @RequestMapping(value = "list/show", method = RequestMethod.GET)
    @ResponseBody
    public RespResult listShowCatalog(Integer channelSectionId, Integer showStatus) {
        List<PmsCatalog> catalogList = catalogService.lambdaQuery()
                .eq(PmsCatalog::getChannelSectionId, channelSectionId)
                .eq(PmsCatalog::getParentId,0)
                .eq(PmsCatalog::getShowStatus,showStatus)
                .eq(PmsCatalog::getStatus,1)
                .list();
        List<PmsCatalogInfo> infoList = to(catalogList);
        return handleResult(infoList);
    }


    //@ApiOperation("获取指定的栏目分类")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    @ResponseBody
    public RespResult getCatalog(Long id) {
        return handleResult(catalogService.getById(id));
    }
    //@ApiOperation("根据id获取指定分类的全部下级分类")
    @RequestMapping(value = "list/cata", method = RequestMethod.GET)
    @ResponseBody
    public RespResult findPmsCataLogByParent(Long parentId) {
        List<PmsCatalog > log = catalogService.lambdaQuery().eq(PmsCatalog::getParentId,parentId).list();
        return handleResult(log);
    }

    //@ApiOperation("更新栏目分类")
    @RequestMapping(value = "save", method = RequestMethod.POST)
    @ResponseBody
    public RespResult saveCatalog(@RequestBody PmsCatalog catalog) {
        Boolean catalogId = catalogService.saveOrUpdate(catalog);
        return handleResult(catalog);
    }

    public List<PmsCatalogInfo> to(List<PmsCatalog> catalogs) {
        if (catalogs == null) {
            return null;
        }

        List<PmsCatalogInfo> list = new ArrayList<PmsCatalogInfo>(catalogs.size());
        for (PmsCatalog pmsCatalog : catalogs) {
            list.add(toCatalogInfo(pmsCatalog));
        }

        return list;
    }

    public PmsCatalogInfo toCatalogInfo(PmsCatalog catalog) {
        if (catalog == null) {
            return null;
        }

        PmsCatalogInfo pmsCatalogInfo = new PmsCatalogInfo();
        pmsCatalogInfo.setId(catalog.getId());
        pmsCatalogInfo.setChannelSectionId(catalog.getChannelSectionId());
        pmsCatalogInfo.setCreatedBy(catalog.getCreatedBy());
        pmsCatalogInfo.setCreatedTime(catalog.getCreatedTime());
        pmsCatalogInfo.setDescription(catalog.getDescription());
        pmsCatalogInfo.setIcon(catalog.getIcon());
        pmsCatalogInfo.setKeywords(catalog.getKeywords());
        pmsCatalogInfo.setLevel(catalog.getLevel());
        pmsCatalogInfo.setMemberId(catalog.getMemberId());
        pmsCatalogInfo.setName(catalog.getName());
        pmsCatalogInfo.setParentId(catalog.getParentId());
        pmsCatalogInfo.setShowStatus(catalog.getShowStatus());
        pmsCatalogInfo.setSort(catalog.getSort());
        pmsCatalogInfo.setWidth(catalog.getWidth());
        pmsCatalogInfo.setIsMoudle(catalog.getIsMoudle());
        pmsCatalogInfo.setMoudleName(catalog.getMoudleName());
        return pmsCatalogInfo;
    }

    private List<PmsCatalogInfo> loadChildren(List<PmsCatalogInfo> children, Integer showStatus) {
        for (PmsCatalogInfo info : children) {
            if (showStatus == null) {
                List<PmsCatalog> catalogs = catalogService.lambdaQuery()
                        .eq(PmsCatalog::getChannelSectionId, info.getChannelSectionId())
                        .eq(PmsCatalog::getParentId,info.getId())
                        .orderByAsc(PmsCatalog::getSort)
                        .list();
                List<PmsCatalogInfo> infoList = to(catalogs);
                info.setChildren(infoList);
            } else {
                List<PmsCatalog> catalogs = catalogService.lambdaQuery()
                        .eq(PmsCatalog::getChannelSectionId, info.getChannelSectionId())
                        .eq(PmsCatalog::getParentId,info.getId())
                        .eq(PmsCatalog::getShowStatus,showStatus)
                        .orderByAsc(PmsCatalog::getSort)
                        .list();
                List<PmsCatalogInfo> infoList = to(catalogs);
                info.setChildren(infoList);
            }

            loadChildren(info.getChildren(), showStatus);
        }

        return children;
    }
}

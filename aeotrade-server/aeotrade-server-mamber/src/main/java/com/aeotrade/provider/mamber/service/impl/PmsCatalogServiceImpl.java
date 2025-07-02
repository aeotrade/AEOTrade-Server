package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.PmsCatalog;
import com.aeotrade.provider.mamber.entity.PmsCatalogDetail;
import com.aeotrade.provider.mamber.feign.AdminFeign;
import com.aeotrade.provider.mamber.feign.WeixinFeign;
import com.aeotrade.provider.mamber.mapper.PmsCatalogMapper;
import com.aeotrade.provider.mamber.service.PmsCatalogDetailService;
import com.aeotrade.provider.mamber.service.PmsCatalogService;
import com.aeotrade.provider.mamber.vo.CloudAppDtoID;
import com.aeotrade.provider.mamber.vo.PmsCatalogInfo;
import com.aeotrade.provider.mamber.vo.PmsDynamicConditionParm;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.JacksonUtil;
import com.aeotrade.utlis.ToolUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 16:07
 */
@Service
public class PmsCatalogServiceImpl extends ServiceImpl<PmsCatalogMapper, PmsCatalog> implements PmsCatalogService {

    @Autowired
    private PmsCatalogDetailService detailMapper;
    @Autowired
    private WeixinFeign weixinFeign;
    @Autowired
    private AdminFeign adminFeign;
    @Override
    public Integer delRedis(Long id) {
        PmsCatalog pmsCatalog = this.getById(id);
        if (pmsCatalog != null && pmsCatalog.getChannelSectionId() != null) {
            return pmsCatalog.getChannelSectionId();
        }
        return null;
    }

    @Override
    @CacheEvict(cacheNames = {"pms_list"}, key = "#channelSectionId")
    public long del(Long id, Integer integer) {
        this.removeById(id);
        return 1;
    }

    @Override
    public long dels(Long id) {
        this.removeById(id);
        return 1;
    }

    @Override
    public List<PmsCatalogInfo> appendDetail(List<PmsCatalogInfo> catalogList) {
        catalogList.forEach(catalog -> {
            List<PmsCatalogDetail> list = detailMapper.lambdaQuery().eq(PmsCatalogDetail::getCatalogId, catalog.getId())
                    .eq(PmsCatalogDetail::getSectionId, catalog.getChannelSectionId()).list();
            PmsCatalogDetail detail =list.size()>0?list.get(0):null;
            catalog.setDetail(detail);
            if (detail != null) {
                if (StringUtils.isNotEmpty(detail.getContent()) && !detail.getContent().equals("[]")) {
                    String tran = tran(detail.getContent());
                    catalog.getDetail().setContent(tran);
                }
            }
            if (catalog.getChildren() != null && !catalog.getChildren().isEmpty()) {
                appendDetail(catalog.getChildren());
            }
        });
        return catalogList;
    }


    @Override
    public void appendDetailId(List<PmsCatalogInfo> catalogList) {
        catalogList.forEach(catalog -> {
            List<PmsCatalogDetail> list = detailMapper.lambdaQuery().eq(PmsCatalogDetail::getCatalogId, catalog.getId())
                    .eq(PmsCatalogDetail::getSectionId, catalog.getChannelSectionId()).list();
            PmsCatalogDetail detail = !list.isEmpty() ?list.get(0):null;
            catalog.setDetail(detail);
            if (catalog.getChildren() != null && !catalog.getChildren().isEmpty()) {
                appendDetailId(catalog.getChildren());
            }
        });
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

    private String tran(String content) {
        try {
            JSONObject jsonObject = JacksonUtil.parseJson(content, JSONObject.class);
            if (jsonObject == null) return "";
            if (jsonObject.containsKey("dataType")) {

                String fieldName = jsonObject.getString("fieldName");
                List<String> list = Splitter.on(",").trimResults().splitToList(fieldName);
                //转换 字段名称
                StringBuilder builder = new StringBuilder();
                for (String s3 : list) {
                    String s = ToolUtils.humpToLine2(s3);
                    builder.append(s + ",");
                }
                //字段名
                String substring = builder.substring(0, builder.toString().length() - 1);

                PmsDynamicConditionParm pmsParm = new PmsDynamicConditionParm();
                pmsParm.setFieldNames(substring);
                pmsParm.setId(jsonObject.getString("id"));
                pmsParm.setSort(ToolUtils.humpToLine2(jsonObject.getString("sort")));
                pmsParm.setType(jsonObject.getInteger("type"));
                pmsParm.setTableName(jsonObject.getString("tableName"));

                if (jsonObject.getInteger("type") == 12) {
                    /**综合查询*/

                    CloudAppDtoID parameter = JSONObject.parseObject(content, CloudAppDtoID.class);
                    if (!parameter.getType().equals(12)) {
                        throw new AeotradeException("");
                    }
                    if (!CommonUtil.isEmpty(parameter.getIds())) {
                        Map<String, Integer> ids = parameter.getIds();
                        StringBuilder appIds = new StringBuilder();
                        StringBuilder cloudAppIds = new StringBuilder();
                        for (String key : ids.keySet()) {
                            Integer type = ids.get(key);
                            if (type.equals(9)) {
                                appIds.append(key).append(",");
                            } else if (type.equals(10)) {
                                cloudAppIds.append(key).append(",");
                            }
                        }
                        List<Document> category = new ArrayList<>();
                        List<Document> r = new ArrayList<>();
                        List<Long> idsList = Arrays.stream(parameter.getId().split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                        for (Long id : idsList) {
                            Document dcument = findDcument(id, category);
                            if (!CommonUtil.isEmpty(dcument)) {
                                r.add(dcument);
                            }
                        }

                        return JSON.toJSONString(r);
                    }
                    System.out.println(parameter);

                }
                if (jsonObject.getInteger("type") == 3) {
                    /**资讯表*/
                    RespResult member = weixinFeign.findMember(pmsParm.getFieldNames(), pmsParm.getId(), pmsParm.getSort(), pmsParm.getType(), pmsParm.getTableName(), 0L, 1000L);
                    return JSON.toJSONString(member.getResult());

                }
                if (jsonObject.getInteger("type") == 6 || jsonObject.getInteger("type") == 2) {
                    /**查企业表*/
                    RespResult member = adminFeign.findMember(pmsParm.getFieldNames(), pmsParm.getId(), pmsParm.getSort(), pmsParm.getType(), pmsParm.getTableName());
                    return JSON.toJSONString(member.getResult());
                }
                return "";
            }
        } catch (Exception e) {
            return "";
        }
        return "";
    }
    private Document findDcument(Long id, List<Document> category) {
        for (Document ids : category) {
            if (ids.getLong("id").equals(id)) {
                return ids;
            }
        }
        return null;
    }
}

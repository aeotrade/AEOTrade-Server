package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.PmsCatalogDetail;
import com.aeotrade.provider.mamber.entity.WxUcd;
import com.aeotrade.provider.mamber.mapper.PmsCatalogDetailMapper;
import com.aeotrade.provider.mamber.service.AppCloudService;
import com.aeotrade.provider.mamber.service.PmsAppService;
import com.aeotrade.provider.mamber.service.PmsCatalogDetailService;
import com.aeotrade.provider.mamber.service.WxCatCudService;
import com.aeotrade.provider.mamber.vo.CloudAppDtoID;
import com.aeotrade.provider.mamber.vo.PmsCatalogDetailVO;
import com.aeotrade.provider.mamber.vo.PmsDynamicConditionParm;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.suppot.RespResultMapper;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.JacksonUtil;
import com.aeotrade.utlis.ToolUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 15:53
 */
@Service
public class PmsCatalogDetailServiceImpl extends ServiceImpl<PmsCatalogDetailMapper, PmsCatalogDetail> implements PmsCatalogDetailService {
    @Autowired
    private PmsCatalogDetailService pmsCatalogDetailService;
    @Autowired
    private WxCatCudService wxCatCudService;
    @Autowired
    private AppCloudService appCloudService;
    @Autowired
    private PmsAppService pmsAppService;

    @Override
    public RespResult listCatalogDetails(long catalogId, Page page) {
        List<PmsCatalogDetail> pmsCatalogDetails = pmsCatalogDetailService.lambdaQuery().eq(PmsCatalogDetail::getCatalogId, catalogId).list();
        if (CommonUtil.isEmpty(pmsCatalogDetails))
            return RespResultMapper.wrap(200, "未查询到数据", Optional.ofNullable(pmsCatalogDetails).orElseGet(ArrayList::new));
        List<PmsCatalogDetailVO> voList = new ArrayList<>();
        PmsCatalogDetail pmsCatalogDetai = pmsCatalogDetails.get(0);
        if (pmsCatalogDetai == null)
            return RespResultMapper.wrap(200, "未查询到数据", Optional.of(voList).orElseGet(ArrayList::new));
        PmsCatalogDetailVO pmsCatalogDetail = new PmsCatalogDetailVO();
        BeanUtils.copyProperties(pmsCatalogDetai, pmsCatalogDetail);
        if (StringUtils.isEmpty(pmsCatalogDetail.getContent()))
            return RespResultMapper.wrap(200, "未查询到数据", Optional.of(voList).orElseGet(ArrayList::new));
        try {

            CloudAppDtoID parameter = JSONObject.parseObject(pmsCatalogDetail.getContent(), CloudAppDtoID.class);
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
                if (!CommonUtil.isEmpty(cloudAppIds)) {
                    if (StringUtils.isNotEmpty(cloudAppIds.substring(0, cloudAppIds.toString().length() - 1))) {
                        category = appCloudService.findAppList("substring",
                                cloudAppIds.substring(0, cloudAppIds.toString().length() - 1), "id", 0,
                                "app_cloud", page.getSize(), page.getCurrent());
                        category.forEach(i -> {
                            i.put("form", 10);
                        });
                        category.addAll(category);

                    }
                }
                List<Document> vo = new ArrayList<>();
                if (!CommonUtil.isEmpty(appIds)) {
                    if (StringUtils.isNotEmpty(appIds.substring(0, appIds.toString().length() - 1))) {
                        if (page == null) {
                            vo = pmsAppService.findDynamicConditionist("id,appName,appLog,url,subhead,authStatus,createdTime",
                                    "pms_app", "sort", "");
                        } else {
                            vo = pmsAppService.findDynamicConditionistPage("id,app_name,app_log,url,subhead,auth_status,created_time",
                                    "pms_app"
                                    , "sort", appIds.substring(0, appIds.toString().length() - 1), page.getCurrent(),
                                    page.getSize());
                        }
                    }
                }
                if (!CommonUtil.isEmpty(vo)) {
                    vo.forEach(i -> {
                        i.put("form", 9);
                    });
                    category.addAll(vo);
                }
                List<Document> r = new ArrayList<>();
                List<Long> idsList = Arrays.stream(parameter.getId().split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                for (Long id : idsList) {
                    Document dcument = findDcument(id, category);
                    if (!CommonUtil.isEmpty(dcument)) {
                        r.add(dcument);
                    }
                }

                pmsCatalogDetail.setContent(JSON.toJSONString(r));
                pmsCatalogDetail.setDataType(1);
                voList.add(pmsCatalogDetail);
                return RespResultMapper.ok(voList);

            }
            System.out.println(parameter);
        } catch (Exception e) {
            try {
                JSONObject jsonObject = JacksonUtil.parseJson(pmsCatalogDetail.getContent(), JSONObject.class);

                if (jsonObject == null)
                    return RespResultMapper.wrap(200, "JSON解析失败", Optional.of(voList).orElseGet(ArrayList::new));
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
                    if (jsonObject.getInteger("type") == 3) {
                        /**资讯表*/
                        List<Document> aiMember = wxCatCudService.findAIMember(pmsParm.getFieldNames(), pmsParm.getId(), pmsParm.getSort(), pmsParm.getType(), pmsParm.getTableName(), (int) page.getSize(),
                                (int) page.getCurrent());
                        RespResult<List<Document>> member = RespResultMapper.wrap(RespResult.SUCCESS_CODE, "操作成功", aiMember);
                        //按着存储格式，调整
                        pmsCatalogDetail.setContent(member.getResult()!=null?JSON.toJSONString(member.getResult()):"[]");
                        pmsCatalogDetail.setDataType(1);
                        voList.add(pmsCatalogDetail);
                        return RespResultMapper.ok(voList);

                    }
                    if (jsonObject.getInteger("type") == 6 || jsonObject.getInteger("type") == 2) {
                        /**查企业表*/
                        List<Document> dynamicConditionist =pmsAppService.findDynamicConditionist(pmsParm.getFieldNames(),pmsParm.getTableName(),pmsParm.getSort(),pmsParm.getId());
                        List<Document> vo = new ArrayList<>();
                        dynamicConditionist.forEach(i -> {
                            Document d = new Document();
                            d.put("content_type", jsonObject.getInteger("type"));
                            d.put("id", i.getLong("id"));
                            d.put("memberName", i.getString("member_name"));
                            d.put("pic", i.getString("logo_img"));
                            vo.add(d);
                        });
                        pmsCatalogDetail.setContent(JSON.toJSONString(vo));
                        pmsCatalogDetail.setDataType(1);
                        voList.add(pmsCatalogDetail);
                        return RespResultMapper.ok(voList);
                    }
                    List<Document> vo = null;
                    String id = jsonObject.getString("id");
                    if (StringUtils.isNotEmpty(id)) {
                        String[] split = id.split(",");
                        pmsCatalogDetail.setTotal(split.length);
                    }
                    if (CommonUtil.isEmpty(vo)) {
                        pmsCatalogDetail.setDataType(1);
                        voList.add(pmsCatalogDetail);
                        return RespResultMapper.ok(voList);
                    }
                    //转换
                    String json = DoCumentToJSON(vo, jsonObject.getInteger("type"));
                    pmsCatalogDetail.setContent(json);
                    pmsCatalogDetail.setDataType(1);

                    voList.add(pmsCatalogDetail);
                    return RespResultMapper.ok(voList);
                }
            } catch (Exception e1) {
                return RespResultMapper.ok(pmsCatalogDetails);
            }
        }
        return null;
    }

    private String DoCumentToJSON(List<Document> vo, Integer type) {
        List<Document> list = new ArrayList<>();
        //app
        if (type == 9) {
            vo.forEach(i -> {
                Document d = new Document();
                d.put("content_type", type);
                d.put("id", i.getLong("id"));
                d.put("name", i.getString("app_name"));
                d.put("pic", i.getString("app_log"));
                d.put("url", i.getString("url"));
                d.put("subTitle", i.getString("subhead"));
                d.put("createdTime", i.getDate("created_time"));
                d.put("authStatus", i.getInteger("auth_status"));
                list.add(d);
            });
            return JSON.toJSONString(list);
            //店铺
        } else if (type == 7 || type == 8) {
            vo.forEach(i -> {
                Document d = new Document();
                d.put("content_type", type);
                d.put("id", i.getLong("id"));
                d.put("shopName", i.getString("shop_name"));
                d.put("shopDescription", i.getString("shop_description"));
                d.put("logoImg", i.getString("shop_logo"));
                list.add(d);
            });
            return JSON.toJSONString(list);
            //商品 服务
        } else if (type == 1 || type == 5) {
            vo.forEach(i -> {
                Document d = new Document();
                d.put("content_type", type);
                d.put("id", i.getLong("id"));
                d.put("name", i.getString("name"));
                d.put("pic", i.getString("pic"));
                d.put("subTitle", i.getString("sub_title"));
                list.add(d);
            });
            return JSON.toJSONString(list);
        }
        return "";
    }

    @Override
    public RespResult pageListCatalogDetails(long catalogId, Integer pageSize, Integer pageNum) {
        List<PmsCatalogDetail> pmsCatalogDetails = this.lambdaQuery().eq(PmsCatalogDetail::getCatalogId,catalogId).list();
        if(CommonUtil.isEmpty(pmsCatalogDetails)) return RespResultMapper.wrap(200,"未查询到数据", Optional.ofNullable(pmsCatalogDetails).orElseGet(ArrayList::new));
        PmsCatalogDetail pmsCatalogDetai = pmsCatalogDetails.get(0);
        if(pmsCatalogDetai==null ) return RespResultMapper.wrap(200,"未查询到数据", Optional.empty().orElseGet(ArrayList::new));
        if( StringUtils.isEmpty(pmsCatalogDetai.getContent())) return RespResultMapper.wrap(200,"未查询到数据",Optional.ofNullable(null).orElseGet(ArrayList::new));
        try {
            JSONObject jsonObject = JacksonUtil.parseJson(pmsCatalogDetai.getContent(), JSONObject.class);
            if(jsonObject==null) return RespResultMapper.wrap(200,"JSON解析失败",Optional.empty().orElseGet(ArrayList::new));
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
            Page<WxUcd> pageAIMember = wxCatCudService.findPageAIMember(pmsParm.getFieldNames(), pmsParm.getId(), pmsParm.getSort(), pmsParm.getType(), pmsParm.getTableName(), pageSize, pageNum);
            return RespResultMapper.wrap(RespResult.SUCCESS_CODE, "操作成功", pageAIMember);
        }catch (Exception e){
            return RespResultMapper.ok( pmsCatalogDetails);
        }
    }

    private Document findDcument(Long id, List < Document > category){
        for (Document ids : category) {
            if (ids.getLong("id").equals(id)) {
                return ids;
            }
        }
        return null;
    }

}

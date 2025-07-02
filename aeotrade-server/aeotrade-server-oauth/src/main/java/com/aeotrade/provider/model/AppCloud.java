package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 应用表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("app_cloud")
public class AppCloud {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用图片
     */
    private String appLogo;

    /**
     * 企业ID
     */
    private Long memberId;

    /**
     * 副标题
     */
    private String subhead;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 应用链接
     */
    private String url;

    /**
     * 是否需要认证,1需要,0不需要
     */
    private Integer isAuth;

    /**
     * 产品详情
     */
    private String productDetail;

    /**
     * 使用说明
     */
    private String productInstruction;

    /**
     * 应用分类ID
     */
    private Long appCategoryCid;

    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 应用父类型id
     */
    private Long appParentCategoryId;

    /**
     * 应用分类名称
     */
    private String appCategoryName;

    /**
     * 推送说明
     */
    private String pushGuide;

    /**
     * api说明
     */
    private String apiGuide;

    /**
     * excel资源获取（购买后）
     */
    private String excelSourcePayAfter;

    /**
     * excel资源获取（购买前）
     */
    private String excelSourcePayBefore;

    /**
     * 店铺类型
     */
    private String shopType;

    /**
     * 上架状态0->下架1->上架
     */
    private Integer publishStatus;

    /**
     * erpid
     */
    private String erpAppId;

    /**
     * 定制说明
     */
    private String customizationGuide;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 是否企业专用 1-是 0-否
     */
    private Integer isEntSpecialUse;

    /**
     * 应用点击数量
     */
    private Integer appPv;

    /**
     * 应用购买数量
     */
    private Integer appPayCount;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 采集数据来源
     */
    private String sources;

    /**
     * 版本号
     */
    private String version;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否显示
     */
    private Integer isShow;

    /**
     * 是否归档
     */
    private Integer isArchive;

    /**
     * 管理后台地址
     */
    private String manageUrl;

    /**
     * APPID
     */
    private String clientId;

    /**
     * 目标跳转地址或标识
     */
    private String state;

    /**
     * 1、无认证 2、oauth2
     */
    private Integer isRequertType;

    /**
     * 创建人
     */
    private String createBy;
}

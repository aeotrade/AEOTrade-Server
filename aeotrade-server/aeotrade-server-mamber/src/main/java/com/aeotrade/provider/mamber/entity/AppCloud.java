package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 应用表
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
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
     * 创建人
     */
    private String createdBy;
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
     * 上架状态  0->审核中  1->已上架  2 ->已下架
     */
    private Integer publishStatus;


    /**
     * 删除
     */
    private Integer status;


    /**
     * 排序
     */
    private Integer sort;


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
     * 应用类型0公开应用1自建应用2推荐应用
     */
    private Integer appType;

    /**
     * 是否打开新窗口 0否 1是
     */
    private Integer isWindows;
    /**
     * 会员类型集合
     */
    @TableField(exist = false)
    private List<AppVipType> vipTypeId;

    /**
     * 应用类目集合
     */
    @TableField(exist = false)
    private List<AppCategory> appCategory;
    /**
     * 企业名称
     */
    @TableField(exist = false)
    private String memberName;

}

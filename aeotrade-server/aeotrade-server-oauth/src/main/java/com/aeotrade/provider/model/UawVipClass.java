package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 会员等级表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uaw_vip_class")
public class UawVipClass {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员等级名称
     */
    private String className;

    /**
     * 所属会员类型id
     */
    private Long typeId;

    /**
     * 会员等级介绍
     */
    private String introduce;

    /**
     * 备注（开通方式）
     */
    private String remark;

    /**
     * 描述开通方式
     */
    private String description;

    /**
     * 图标（小钻石）
     */
    private String ico;

    /**
     * 图片（大）
     */
    private String picture;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 对应工作台链接
     */
    private String workbenchUrl;

    /**
     * 会员使用时间
     */
    private Integer term;

    /**
     * 时间单位
     */
    private String termUnit;

    /**
     * 价格
     */
    private Double price;

    /**
     * 价格单位
     */
    private String priceUnit;

    /**
     * 开通条件(是否需要个人实名认证)
     */
    private Integer openingCondition;

    /**
     * 顺序
     */
    private Integer sort;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 会员等级编号
     */
    private String classSerialNumber;

    /**
     * 是否启用0否1是
     */
    private Integer isStartUsing;

    /**
     * 单证存储容量
     */
    private Integer storeNumber;

    /**
     * 单证存储容量单位
     */
    private String storeUnit;
}

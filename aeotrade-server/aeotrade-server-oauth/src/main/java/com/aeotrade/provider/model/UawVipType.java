package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 会员分类表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uaw_vip_type")
public class UawVipType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员类型名称
     */
    private String typeName;

    /**
     * 会员类型分组id
     */
    private Long groupId;

    /**
     * 未选中
     */
    private String ico;

    /**
     * 选中
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
     * 类型描述
     */
    private String description;

    /**
     * 对应工作台
     */
    private Long workbench;

    /**
     * 是否需要审核0否1是
     */
    private Integer isAuditRequired;

    /**
     * 默认开通会员等级id
     */
    private Long defaultVipClassId;

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
     * 是否为默认会员0否1是
     */
    private Integer isDefaultVip;

    /**
     * 工作台标识
     */
    private String code;

    /**
     * 会员类型状态0启用1禁用
     */
    private Integer vipTypeStatus;

    /**
     * 店铺类型名称
     */
    private String shopTypeName;

    /**
     * 权益对比图
     */
    private String rightsIco;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 关联类型id
     */
    private String relevancyTypeId;
}

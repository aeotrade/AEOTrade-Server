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
 * 应用分类表
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
@Getter
@Setter
@TableName("app_category")
public class AppCategory {

    @TableId(value = "cid", type = IdType.AUTO)
    private Long cid;


    /**
     * 分类名称
     */
    private String appTypeName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 分类状态
     */
    private Integer typeStatus;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;


    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 修改时间
     */
    private LocalDateTime updatedTime;

    /**
     * 图片
     */
    private String img;

    /**
     * 描述
     */
    private String description;

    /**
     * 会员类型id
     */
    private Long vipTypeId;

    /**
     * 会员类型名称
     */
    private String vipTypeName;

    /**
     * 应用集合
     */
    @TableField(exist = false)
    private List<AppCloud> appCloudList;
}

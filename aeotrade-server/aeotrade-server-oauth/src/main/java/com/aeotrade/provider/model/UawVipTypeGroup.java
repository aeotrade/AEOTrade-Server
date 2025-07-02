package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 会员类型分组表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uaw_vip_type_group")
public class UawVipTypeGroup {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员类型分组名称
     */
    private String groupName;

    /**
     * 所属适用于0个人/1企业
     */
    private Integer apply;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否为默认会员0否1是
     */
    private Integer isDefaultVip;

    /**
     * 入驻图片
     */
    private String ico;

    /**
     * 所属工作台
     */
    private String workbench;
}

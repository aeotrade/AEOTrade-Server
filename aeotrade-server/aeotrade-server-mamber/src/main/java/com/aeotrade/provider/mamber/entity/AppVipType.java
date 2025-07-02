package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 应用与会员类型关联表
 * </p>
 *
 * @author aeo
 * @since 2023-10-25
 */
@Getter
@Setter
@TableName("app_vip_type")
public class AppVipType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 应用id
     */
    private Long cloudId;

    /**
     * 会员类型id
     */
    private Long vipTypeId;

    /**
     * 会员类型名称
     */
    private String vipTypeName;
}

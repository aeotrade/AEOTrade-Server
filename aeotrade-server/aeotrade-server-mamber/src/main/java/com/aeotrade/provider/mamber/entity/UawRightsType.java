package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 权益类型表
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_rights_type")
public class UawRightsType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权益类型名称
     */
    private String rightsTypeName;

    /**
     * 权益状态
     */
    private Integer rightsTypeStutas;

    /**
     * 权益描述
     */
    private String description;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 权益项图标
     */
    private String ico;
}

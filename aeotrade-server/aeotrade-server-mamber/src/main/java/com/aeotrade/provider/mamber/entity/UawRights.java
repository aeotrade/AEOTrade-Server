package com.aeotrade.provider.mamber.entity;

import com.aeotrade.provider.mamber.vo.RightsNameVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 权益项表
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_rights")
public class UawRights extends RightsNameVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权益名称
     */
    private String rightsName;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 资源id
     */
    private Long resourceId;

    /**
     * 图标1
     */
    private String ico;

    /**
     * 图标2
     */
    private String icoT;

    /**
     * 权益分类id
     */
    private Long rightsTypeId;

    /**
     * 使用方式0线上1线下
     */
    private Integer usePattern;

    /**
     * 权益项描述
     */
    private String description;

    /**
     * 应用地址
     */
    private String resourceName;
}

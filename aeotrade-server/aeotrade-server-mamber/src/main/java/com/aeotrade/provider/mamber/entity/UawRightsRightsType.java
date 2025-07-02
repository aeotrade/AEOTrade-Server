package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 权益项和权益类型关联表
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_rights_rights_type")
public class UawRightsRightsType {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权益id
     */
    private Long rightsId;

    /**
     * 权益类型id
     */
    private Long rightsTypeId;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 会员等级id
     */
    private Long vipClassId;
}

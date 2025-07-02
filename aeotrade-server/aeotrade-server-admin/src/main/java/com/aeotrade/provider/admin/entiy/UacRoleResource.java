package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 后台角色资源关系表
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("uac_role_resource")
public class UacRoleResource {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 资源ID
     */
    private Long resourceId;
}

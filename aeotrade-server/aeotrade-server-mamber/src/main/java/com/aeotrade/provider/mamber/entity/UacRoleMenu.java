package com.aeotrade.provider.mamber.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * UacRoleMenu后台角色菜单关系表
 */

@Data
@TableName("uac_role_menu")
public class UacRoleMenu {


    private Long id;


    private Long menuId;


    private Long roleId;

}


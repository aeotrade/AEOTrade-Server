package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: 吴浩
 * @Date: 2025/3/19 11:19
 */
@Getter
@Setter
@TableName("app_role")
public class AppRole {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业ID
     */
    private Long memberId;

    /**
     * 应用ID
     */
    private Long appId;

    /**
     * 适用角色
     */
    private String userRole;
    /**
     * 可见范围(
     * 1全部员工可用
     * 2部分员工可用
     * 3全部员工都不可用
     * )
     */
    private int visibleRange;
    /**
     * 类目名称
     */
    @TableField(exist = false)
    private String appTypeName;
    /**
     * 应用名称
     */
    @TableField(exist = false)
    private String appName;
    /**
     * 应用图标
     */
    @TableField(exist = false)
    private String appLogo;
}

package com.aeotrade.provider.mamber.entity;

import com.aeotrade.provider.mamber.vo.MenuMetaDto;
import com.aeotrade.provider.mamber.vo.WorkbenchVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_workbench_menu")
@EqualsAndHashCode
public class UawWorkbenchMenu {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 菜单父id
     */
    private Long parentId;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 所属工作台id或应用id
     */
    private Long workbenchId;

    /**
     * 菜单名称
     */
    private String title;

    /**
     * 平台类型(1.服务平台2.运营平台3.独立应用)
     */
    private Integer platformType;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 是否为默认菜单
     */
    private Integer isDefault;

    /**
     * 资源类型1.菜单2.按钮
     */
    private Integer type;

    /**
     * 是否缓存
     */
    private Integer cache;

    /**
     * 组件
     */
    private String component;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否隐藏
     */
    private Integer hidden;

    /**
     * 是否内嵌
     */
    private Integer iframe;

    /**
     * 内嵌框架地址
     */
    private String iframeUrl;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 地址
     */
    private String path;

    /**
     * 权限
     */
    private String permission;

    /**
     * 重定向地址
     */
    private String redirect;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否显示
     */
    private Integer isHidden;

    /**
     * 按钮名称
     */
    private String buttonName;

    /**
     * 按钮地址
     */
    private String buttonPath;

    /**
     * 页面所属系统
     */
    private String webSys;

    /**
     * 按钮类型
     */
    private String buttonType;

    /**
     * 是否分组
     */
    private Integer isGroup;

    @TableField(exist = false)
    private List<WorkbenchVo> children;
}

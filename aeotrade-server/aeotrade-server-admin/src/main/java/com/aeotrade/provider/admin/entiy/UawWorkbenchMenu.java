package com.aeotrade.provider.admin.entiy;

import com.aeotrade.provider.admin.adminVo.MenuMetaDto;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("uaw_workbench_menu")
public class UawWorkbenchMenu {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;


    ////@ApiModelProperty(value="创建时间", allowEmptyValue=true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    //@ApiModelProperty(value="菜单图标", allowEmptyValue=true)
    private String icon;


    //@ApiModelProperty(value="是否内嵌", allowEmptyValue=true)
    private int iframe;

    //@ApiModelProperty(value="标题描述", allowEmptyValue=true)

    private String buttonName;

    //@ApiModelProperty(value="标题描述", allowEmptyValue=true)

    private String buttonPath;

    //@ApiModelProperty(value="菜单父id", allowEmptyValue=true)

    private Long parentId;


    //@ApiModelProperty(value="顺序", allowEmptyValue=true)

    private Integer sort;


    //@ApiModelProperty(value="工作台id", allowEmptyValue=true)

    private Long workbenchId;

    //@ApiModelProperty(value="资源类型1.菜单2.按钮", allowEmptyValue=true)

    private int type;

    //@ApiModelProperty(value="是否缓存", allowEmptyValue=true)

    private int cache;

    //@ApiModelProperty(value="组件", allowEmptyValue=true)

    private String component;

    //@ApiModelProperty(value="菜单是否可见", allowEmptyValue=true)
    private int hidden;

    //@ApiModelProperty(value="内嵌框架地址", allowEmptyValue=true)

    private String iframeUrl;

    //@ApiModelProperty(value="层级", allowEmptyValue=true)

    private int level;



    //@ApiModelProperty(value="菜单名称", allowEmptyValue=true)

    private String name;

    //@ApiModelProperty(value="权限", allowEmptyValue=true)

    private String path;

    //@ApiModelProperty(value="权限", allowEmptyValue=true)

    private String permission;

    //@ApiModelProperty(value="重定向地址", allowEmptyValue=true)

    private String redirect;

    //@ApiModelProperty(value="标题描述", allowEmptyValue=true)

    private String title;

    //@ApiModelProperty(value="平台类型(1.服务平台2.运营平台3.独立应用)", allowEmptyValue=true)

    private int platformType;

    //@ApiModelProperty(value="是否为默认菜单", allowEmptyValue=true)

    private Integer isDefault;

    //@ApiModelProperty(value="是否隐藏", allowEmptyValue=true)

    private Integer isHidden;

    private Integer status;

    @TableField(exist = false)
    private MenuMetaDto meta;



}
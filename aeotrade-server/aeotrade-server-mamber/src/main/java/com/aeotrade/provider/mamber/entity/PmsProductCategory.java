package com.aeotrade.provider.mamber.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
@Data
@TableName("pms_product_category")
public class PmsProductCategory implements Serializable {
    private Long id;

    //@ApiModelProperty(value = "上机分类的编号：0表示一级分类")
    private Long parentId;

    private String name;

    //@ApiModelProperty(value = "分类级别：0->1级；1->2级")
    private Integer level;

    private Integer productCount;

    private String productUnit;

    //@ApiModelProperty(value = "是否显示在导航栏：0->不显示；1->显示")
    private Integer navStatus;

    //@ApiModelProperty(value = "显示状态：0->不显示；1->显示")
    private Integer showStatus;

    private Integer sort;

    //@ApiModelProperty(value = "图标")
    private String icon;

    private String keywords;

    //@ApiModelProperty(value = "描述")
    private String description;
    
}
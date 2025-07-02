package com.aeotrade.provider.mamber.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_product_category_attribute_relation")
public class PmsProductCategoryAttributeRelation  {
    private Long id;

    private Long productCategoryId;

    private Long productAttributeId;


}
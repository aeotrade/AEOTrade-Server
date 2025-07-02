package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PmsCatalogDetail分类目录详情
 */
@Data
@TableName("pms_catalog_detail")
public class PmsCatalogDetail {

    /**
     * ID
     */
    private Long id;

    /**
     * 分类目录ID
     */
    private Long catalogId;

    /**
     * 栏目ID
     */
    private Integer sectionId;

    /**
     * 内容类型,商品、文章、企业等
     */
    private String contentType;

    /**
     * 内容, 以 JSON 的格式保存
     */
    private String content;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建人
     */
    private Integer createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private Integer updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 删除
     */
    private Integer status;


}


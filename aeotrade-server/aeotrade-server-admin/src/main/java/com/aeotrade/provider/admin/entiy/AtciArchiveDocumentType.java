package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AtciArchiveDocumentType单证类型
 */
@Data
@TableName("atci_archive_document_type")
public class AtciArchiveDocumentType {

    private String categoryCode;


    private Long createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    private String criteria;


    private Long dataTypeTemplateId;


    private String deDuplicationRules;


    private Integer downloadIsShow;


    private Integer downloadSort;


    private Integer enableStatus;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;


    private Integer isShow;


    private Integer isTemplate;


    private String msgType;


    private String name;


    private String nameEn;


    private String node;


    private Integer retentionPeriod;


    private String retentionPeriodUnit;



    private Integer revision;


    private String rules;


    private Integer rulesForm;


    private String rulesType;


    private String saveTable;


    private String secondCategoryCode;


    private Integer sort;


    private Integer status;


    private String templateId;


    private Long unionId;


    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

}


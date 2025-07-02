package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UawAptitudes入驻申请表
 */
@Getter
@Setter
@TableName("uaw_aptitudes")
public class UawAptitudes {

    //@ApiModelProperty(value="审核人", allowEmptyValue=true)
    private String auditor;

    //@ApiModelProperty(value="创建人", allowEmptyValue=true)
    private String createdBy;

    //@ApiModelProperty(value="申请人ID", allowEmptyValue=true)
    private Long createdById;

    //@ApiModelProperty(value="创建时间", allowEmptyValue=true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    //@ApiModelProperty(value="id", allowEmptyValue=true)
    private Long id;

    //@ApiModelProperty(value="企业id", allowEmptyValue=true)
    private Long memberId;

    //@ApiModelProperty(value="企业名称", allowEmptyValue=true)
    private String memberName;

    //@ApiModelProperty(value="企业证件", allowEmptyValue=true)
    private String memberPapers;

    //@ApiModelProperty(value="未通过描述", allowEmptyValue=true)
    private String noPassDescription;

    //@ApiModelProperty(value="乐观锁", allowEmptyValue=true)
    private Integer revision;

    //@ApiModelProperty(value="认证状态", allowEmptyValue=true)
    private Integer sgsStatus;


    private Integer status;

    //@ApiModelProperty(value="更新人", allowEmptyValue=true)
    private Long updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    //@ApiModelProperty(value="企业信用代码", allowEmptyValue=true)
    private String uscc;

    //@ApiModelProperty(value="分组ID", allowEmptyValue=true)
    private Integer vipGroupId;

    //@ApiModelProperty(value="分组名称", allowEmptyValue=true)
    private String vipGroupName;

    //@ApiModelProperty(value="会员类型id", allowEmptyValue=true)
    private Long vipTypeId;

    //@ApiModelProperty(value="类型名称", allowEmptyValue=true)
    private String vipTypeName;

}


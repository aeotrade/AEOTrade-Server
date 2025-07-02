package com.aeotrade.provider.mamber.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2023-05-19 16:11
 */
@Data
public class UawVipMessageVo {

    private String classSerialNumber;

    private java.sql.Timestamp createdTime;

    private java.sql.Timestamp endTime;

    private Long id;
    private Long memberId;

    private Integer revision;

    private Integer sgsStatus;

    private Long staffId;


    private java.sql.Timestamp startTime;


    private Integer status;


    private Long typeId;


    private java.sql.Timestamp updatedTime;


    private Integer userType;

    private String vipDetails;


    private Integer vipStatus;

    // @ApiModelProperty(value="会员等级名称", allowEmptyValue=true)
    private String className;
    // @ApiModelProperty(value="会员类型名称", allowEmptyValue=true)
    private String typeName;
    // @ApiModelProperty(value="会员分组id", allowEmptyValue=true)
    private String groupId;
    // @ApiModelProperty(value="会员分组名称", allowEmptyValue=true)
    private String groupName;
    // @ApiModelProperty(value="企业名称/个人名称", allowEmptyValue=true)
    private String name;
    // @ApiModelProperty(value="企业负责人名称", allowEmptyValue=true)
    private String staffName;
    //@ApiModelProperty(value="邮箱", allowEmptyValue=true)
    private String email;
    // @ApiModelProperty(value="电话", allowEmptyValue=true)
    private String tel;
}

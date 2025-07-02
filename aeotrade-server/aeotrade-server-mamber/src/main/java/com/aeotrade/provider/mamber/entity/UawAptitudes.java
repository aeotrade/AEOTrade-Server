package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 入驻申请表
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Getter
@Setter
@TableName("uaw_aptitudes")
public class UawAptitudes {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员类型id
     */
    private Long vipTypeId;

    /**
     * 企业id
     */
    private Long memberId;

    /**
     * 认证状态
     */
    private Integer sgsStatus;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 企业名称
     */
    private String memberName;

    /**
     * 企业信用代码
     */
    private String uscc;

    /**
     * 企业证件
     */
    private String memberPapers;

    /**
     * 类型名称
     */
    private String vipTypeName;

    /**
     * 分组ID
     */
    private Integer vipGroupId;

    /**
     * 分组名称
     */
    private String vipGroupName;

    /**
     * 未通过描述
     */
    private String noPassDescription;

    /**
     * 审核人
     */
    private String auditor;

    /**
     * 申请人ID
     */
    private Long createdById;
}

package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 认证信息表
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("sgs_apply")
public class SgsApply {

    private Long id;

    private Long memberId;

    /**
     * 0:员工, 1: 企业
     */
    private Integer userType;

    /**
     * 认证类型
     */
    private Integer sgsType;

    /**
     * 认证方式名称
     */
    private String sgsTypeName;

    /**
     * 认证状态(0:未认证,1:认证中,2:认证成功)
     */
    private Integer sgsStatus;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 企业名称
     */
    private String memberName;

    /**
     * 企业统一社会信用代码
     */
    private String uscc;

    /**
     * 备注（不通过理由）
     */
    private String remark;

    @TableField(exist = false)
    private String staffName;
    @TableField(exist = false)
    private String tel;
    @TableField(exist = false)
    private String email;
}

package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uac_erp")
public class UacErp {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 企业联系人名称
     */
    private String staffName;

    /**
     * 企业名称
     */
    private String memberName;

    /**
     * 企业联系人
     */
    private Long memberId;

    /**
     * python规定  0：管理员 1：员工
     */
    private Integer role;

    /**
     * 删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 企业联系人
     */
    private Long staffId;

    /**
     * 企业状态
     */
    private Integer memberStatus;

    /**
     * 标识
     */
    private String code;
}

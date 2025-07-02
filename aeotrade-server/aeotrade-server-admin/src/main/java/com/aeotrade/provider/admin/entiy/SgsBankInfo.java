package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 银行认证
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("sgs_bank_info")
public class SgsBankInfo {

    private Long id;

    /**
     * 企业ID
     */
    private Long memberId;

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
     * uscc
     */
    private String uscc;

    /**
     * 认证信息ID
     */
    private Long sgsApplyId;

    /**
     * 0:个人1:企业
     */
    private Integer userType;

    /**
     * 打款金额
     */
    private String deduction;

    /**
     * 开户行
     */
    private String bankOfDeposit;

    /**
     * 银行账号
     */
    private String bankAccount;

    /**
     * 开户支行
     */
    private String bankSub;

    /**
     * 银行开户名称
     */
    private String bankAccountName;
}

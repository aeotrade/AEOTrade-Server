package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 易速登录注册同步信息表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uac_yisu_info")
public class UacYisuInfo {

    private Long id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 用户电话
     */
    private String userPhone;

    /**
     * 用户邮箱
     */
    private String userEmail;

    /**
     * 单窗ic卡号
     */
    private String icCode;

    /**
     * 单窗账号
     */
    private String swLogNm;

    /**
     * 单窗密码
     */
    private String swLogPw;

    /**
     * 企业名称
     */
    private String tradeName;

    /**
     * 企业海关代码
     */
    private String tradeCode;

    /**
     * 企业社会信用代码
     */
    private String tradeCodeScc;

    /**
     * 1注册2登录3同步
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 是否同步erp成功
     */
    private Integer isSuccess;

    /**
     * 错误日志
     */
    private String errorInfo;
}

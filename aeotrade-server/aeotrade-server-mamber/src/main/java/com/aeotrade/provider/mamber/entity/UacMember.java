package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 企业表
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("uac_member")
public class UacMember {

    /**
     * ID
     */
    private Long id;

    /**
     * 企业名称
     */
    private String memberName;

    /**
     * 企业类型
     */
    private Long kindId;

    /**
     * 企业状态
     */
    private Integer memberStatus;

    /**
     * 企业联系人
     */
    private Long staffId;

    /**
     * 企业联系人名称
     */
    private String staffName;

    /**
     * 乐观锁
     */
    private Integer revision;

    /**
     * 创建人
     */
    private Long createdBy;

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
     * 删除
     */
    private Integer status;

    /**
     * 企业联系人电话
     */
    private String stasfTel;

    /**
     * 下次不再提示,0为未勾选,1为已勾选
     */
    private Integer atpwStatus;

    /**
     * 统一社会信用代码
     */
    private String uscCode;

    /**
     * 企业有效状态
     */
    private String remark;

    /**
     * 企业LOGO图片
     */
    private String logoImg;

    /**
     * 企业邮箱
     */
    private String email;

    /**
     * 企业二维码
     */
    private String qrCode;

    /**
     * 企业成立时间
     */
    private LocalDateTime dateTime;

    /**
     * 法人
     */
    private String legalPerson;

    /**
     * 法人身份证号
     */
    private Long personId;

    /**
     * 法人邮箱
     */
    private String legalPersonEmail;

    /**
     * 法人移动电话
     */
    private String legalPersonMobile;

    /**
     * 法人座机
     */
    private String legalPersonTel;

    /**
     * 用户码
     */
    private String subscriberCode;

    /**
     * 认证状态
     */
    private Integer sgsStatus;

    /**
     * 企业地址
     */
    private String address;

    private String memberClientid;

    /**
     * 是否参与灰度测试
     */
    private Integer isTest;

    /**
     * 灰度测试跳转地址
     */
    private String jumpAddress;
}

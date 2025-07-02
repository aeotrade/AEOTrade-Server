package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 企业认证
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("sgs_cert_info")
public class SgsCertInfo {

    @TableId(value = "id", type = IdType.AUTO)
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
     * 企业证件类型
     */
    private String memberPapersType;

    /**
     * 企业证件附件
     */
    private String memberPapersImg;

    /**
     * 认证人身份
     */
    private String identity;

    /**
     * 法人
     */
    private String legalPerson;

    /**
     * 法人身份证号
     */
    private String personId;

    /**
     * 授权书附件
     */
    private String appendix;
}

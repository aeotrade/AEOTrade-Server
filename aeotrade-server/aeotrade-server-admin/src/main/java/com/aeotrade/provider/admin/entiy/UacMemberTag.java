package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 企业特点
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("uac_member_tag")
public class UacMemberTag {

    /**
     * ID
     */
    private Long id;

    /**
     * 企业表_ID
     */
    private Long atfId;

    /**
     * 企业
     */
    private Long memberId;

    /**
     * 字典
     */
    private Long bizDictId;

    /**
     * 字典项
     */
    private Long bizDictItemId;

    /**
     * 特点
     */
    private String bizDictItemName;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    private Long updatedBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 删除
     */
    private Integer status;
}

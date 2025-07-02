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
 * 后台用户和角色关系表
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("uac_admin_role")
public class UacAdminRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 用户id
     */
    private Long adminId;
    /**
     * 企业id
     */
    private Long memberId;
    /**
     * 租户ID
     */
    private String orgi;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String creater;
}

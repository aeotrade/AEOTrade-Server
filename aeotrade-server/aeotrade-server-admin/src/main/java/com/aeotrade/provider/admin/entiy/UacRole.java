package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 后台用户角色表
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Getter
@Setter
@TableName("uac_role")
public class UacRole {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 后台用户数量
     */
    private Integer adminCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 启用状态：0->禁用；1->启用
     */
    private Integer status;

    private Integer sort;

    /**
     * 代码
     */
    private String code;

    /**
     * 创建人
     */
    private String creater;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatetime;

    /**
     * 租户ID
     */
    private String orgi;

    /**
     * 企业ID
     */
    private String orgid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门
     */
    private String organ;

    /**
     * 所属平台1服务平台2运营平台3独立应用4erp
     */
    private Integer platform;

    /**
     * 是否为模板
     */
    private Integer isModel;

    /**
     * 所属工作台id
     */
    private Long platformId;

    private Integer isDefault;

    @TableField(exist = false)
    private List<String> permissions;
}

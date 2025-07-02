package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 后台认证列表
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("sgs_configuration")
public class SgsConfiguration {

    private Long id;

    /**
     * 认证方式名称
     */
    private String sgsName;

    /**
     * 认证方式图标
     */
    private String ico;

    /**
     * 认证说明
     */
    private String description;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 启用状态0:禁用1:启用
     */
    private Integer sgsStatus;

    /**
     * 1企业,0个人
     */
    private Integer userType;

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
     * 认证类型标识
     */
    private Integer sgsType;
    /**
     * 0:普通认证;1:区块链认证，默认是普通认证
     */
    private Integer authToChain;
    /**
     * 认证机构配置,如果是链上认证，则需要配置认证机构信息
     */
    private String issuerConfig;
}

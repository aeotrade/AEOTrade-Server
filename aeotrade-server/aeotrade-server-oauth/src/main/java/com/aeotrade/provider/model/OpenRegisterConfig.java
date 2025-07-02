package com.aeotrade.provider.model;

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
@TableName("open_register_config")
public class OpenRegisterConfig {

    private Long id;

    private String clientId;

    /**
     * 注册类型  auto_create_all->自动创建企业和员工
     */
    private String tenantType;

    /**
     * 功能 
     */
    private String registerFunction;

    /**
     * 默认开通应用
     */
    private String defaultApp;

    /**
     * 角色权限
     */
    private String role;

    /**
     * 白名单Ip
     */
    private String whiteListIp;

    /**
     * ip限流,
     */
    private String requestCurrentLimit;

    /**
     * 创建企业ID
     */
    private Long createMemberId;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建员工ID
     */
    private Long createStaffId;

    /**
     * 创建员工名称
     */
    private String createStaffName;

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
     * 创建方式 0->运营,1->企业,2->用户
     */
    private Integer createWay;

    /**
     * 对接平台名称
     */
    private String dockingName;

    /**
     * 工作台ID
     */
    private Long workbenchId;

    /**
     * 工作台标识
     */
    private String workbenchCode;

    /**
     * 对接方式,0->作为应用接入第三方平台, 1->第三方应用接入本平台
     */
    private Integer dockingWay;

    /**
     * 授权方式
     */
    private String oauthWay;

    /**
     * 工作台名称
     */
    private String workbenchName;

    /**
     * 授权地址
     */
    private String oauthAddr;

    /**
     * 工作台会员等级ID
     */
    private Long workbenchVipClassid;

    /**
     * 工作会员类型ID
     */
    private Long workbenchVipTypeid;
}

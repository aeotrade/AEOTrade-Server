package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 后台用户表
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Getter
@Setter
@TableName("uac_admin")
public class UacAdmin {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    /**
     * 头像
     */
    private String icon;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 备注信息
     */
    private String note;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 帐号启用状态：0->禁用；1->启用
     */
    private Integer status;

    /**
     * 员工ID
     */
    private Long staffId;

    /**
     * 电话
     */
    private String mobile;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 标签1慧贸OS2运营中心
     */
    private Integer isTab;

    /**
     * 安全级别
     */
    private String secureconf;

    /**
     * 租户ID
     */
    private String orgi;

    /**
     * 最后 一次密码修改时间
     */
    private LocalDateTime passupdatetime;

    /**
     * 是否已删除
     */
    private Byte del;

    /**
     * 工号
     */
    private Byte agent;

    /**
     * 数据状态
     */
    private Byte datastatus;

    /**
     * 启用呼叫中心坐席
     */
    private Byte callcenter;

    /**
     * 系统管理员
     */
    private Byte superadmin;

    /**
     * 管理员
     */
    private Byte admin;

    /**
     * 最大接入访客数量
     */
    private Integer maxuser;

    /**
     * 关注人数
     */
    private Integer fans;

    /**
     * 被关注次数
     */
    private Integer follows;

    /**
     * 积分
     */
    private Integer integral;

    /**
     * 是否登录
     */
    private Byte login;

    /**
     * 北京单一窗口用户唯一标识
     */
    private String singleRes;

    /**
     * 用户ID
     */
    private String userId;
}

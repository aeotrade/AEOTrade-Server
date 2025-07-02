package com.aeotrade.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户接受邀请
 * @Author: yewei
 * @Date: 2020/2/26 9:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxUacStaffDto {
    private String code;

    private String  state;

    //创建人
    private String createdBy;

    //创建时间
    private java.sql.Timestamp createdTime;

    //部门
    private String dept;

    private Long id;

    //登录帐号
    private String loginAccount;

    //登录密码
    private String loginPaswd;

    //企业ID
    private Long memberId;

    //乐观锁
    private Integer revision;

    //角色 管理员 :admin 普通员工:user
    private String role;

    //微信授权状态 未授权 0 ;已授权 1
    private Integer sgsStatus;

    //客服名称
    private String staffName;

    //启用状态
    private String staffStatus;

    //删除
    private Integer status;

    //手机号
    private String tel;

    //更新人
    private String updatedBy;

    //更新时间
    private LocalDateTime updatedTime;

    //头像
    private String wxLogo;

    //微信OpenId
    private String wxOpenid;
}

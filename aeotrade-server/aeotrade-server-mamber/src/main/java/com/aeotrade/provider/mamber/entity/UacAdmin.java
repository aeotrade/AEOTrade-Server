package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UacAdmin后台用户表
 */
@Data
@TableName("uac_admin")
public class UacAdmin {


    private Byte admin;


    private Byte agent;


    private Byte callcenter;


    private LocalDateTime createTime;


    private Byte datastatus;


    private Byte del;


    private String email;


    private Integer fans;


    private Integer follows;


    private String icon;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private Integer integral;


    private Integer isTab;


    private Byte login;


    private LocalDateTime loginTime;


    private Integer maxuser;


    private String mobile;


    private String nickName;


    private String note;


    private String orgi;


    private LocalDateTime passupdatetime;


    private String password;

    private String secureconf;


    private Long staffId;


    private Integer status;


    private Byte superadmin;


    private LocalDateTime updateTime;


    private String username;

}


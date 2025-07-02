package com.aeotrade.provider.mamber.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UacRole后台用户角色表
 */
@TableName("uac_role")
@Data
public class UacRole {


    private Integer adminCount;


    private String code;


    private String creater;

    private LocalDateTime createTime;


    private String description;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private String name;


    private String organ;


    private String orgi;

    private String orgid;

    private Integer sort;

    private Integer status;

    private LocalDateTime updatetime;

    private String username;

    private Integer platform;

    private Integer isModel;

    private Long platformId;

    private Integer isDefault;
}


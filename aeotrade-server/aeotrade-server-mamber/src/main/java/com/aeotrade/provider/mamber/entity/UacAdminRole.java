package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UacAdminRole后台用户和角色关系表
 */

@Data
@TableName("uac_admin_role")
public class UacAdminRole {

    private Long adminId;

    private String creater;

    private LocalDateTime createTime;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long memberId;

    private String orgi;

    private Long roleId;

}


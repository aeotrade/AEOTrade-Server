package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("uac_user")
public class UacUser {

    @TableId("user_id")
    private Long userId;

    private String username;

    private String password;

    private Long deptId;

    private String email;

    private String mobile;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime modifyTime;

    private LocalDateTime lastLoginTime;

    private Integer ssex;

    private Integer isTab;

    private String theme;

    private String avatar;

    private String description;

    /**
     * 员工ID
     */
    private Long staffId;
}

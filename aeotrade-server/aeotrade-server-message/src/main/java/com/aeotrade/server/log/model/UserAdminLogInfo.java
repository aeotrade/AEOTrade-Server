package com.aeotrade.server.log.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 16:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_admin_log_info")
public class UserAdminLogInfo {
    private static final long serialVersionUID = 1L;

    private Long id;

    //页面名称
    private String cPagename;

    //页面url
    private String cUrl;

    //停留时间
    private String durationTime;

    //上一个页面名称
    private String fPagename;

    //上一页页面url
    private String furl;

    //角色
    private String role;

    //用户名称
    private String name;

    //账号
    private String userName;

    //请求时间
    private Timestamp time;

    private String type;

}

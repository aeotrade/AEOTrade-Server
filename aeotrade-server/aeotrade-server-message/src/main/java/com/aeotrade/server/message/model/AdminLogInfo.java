package com.aeotrade.server.message.model;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 16:47
 */
@Data
public class AdminLogInfo {
    private static final long serialVersionUID = 1L;

    private Long id;

    //页面名称
    private String cpagename;

    //页面url
    private String curl;

    //停留时间
    private Long durationtime;

    //上一个页面名称
    private String fpagename;

    //上一页页面url
    private String furl;

    //角色
    private String role;

    //用户名称
    private String name;

    //账号
    private String username;

    //请求时间
    private Long time;

    private String type;

}

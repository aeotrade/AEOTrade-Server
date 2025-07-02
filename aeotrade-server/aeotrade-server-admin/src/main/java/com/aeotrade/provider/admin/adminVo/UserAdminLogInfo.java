package com.aeotrade.provider.admin.adminVo;

import lombok.Getter;
import lombok.Setter;


/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 16:47
 */
@Getter
@Setter
public class UserAdminLogInfo {
   // //@ApiModelProperty(value="页面名称")
    private String cpagename;

    //@ApiModelProperty(value="页面url")
    private String curl;

    //@ApiModelProperty(value="停留时间")
    private Long durationtime;

    //@ApiModelProperty(value="上一个页面名称")
    private String fpagename;

    //@ApiModelProperty(value="上一页页面url")
    private String furl;

    //@ApiModelProperty(value="角色")
    private String role;

    //@ApiModelProperty(value="用户名称")
    private String name;

    //@ApiModelProperty(value="账号")
    private String username;

    //@ApiModelProperty(value="请求时间")
    private Long time;

    private String type;

}

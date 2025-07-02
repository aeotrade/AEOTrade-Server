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
@TableName("aeotrade_user_log_info")
public class AeotradeUserLogInfo {
    private static final long serialVersionUID = 1L;

    private Long id;

    //操作人名称
    private String user_name;

    //操作企业名称
    private String menber_name;

    //页面url
    private String web_url;

    //用户ip地址
    private String ip;

    //请求时间
    private Timestamp request_time;

    //请求url
    private String request_url;

    //请求类型
    private String request_type;

    //请求参数
    private String request_parameter;

    //请求属性
    private String request_nature;

    //请求接口名称
    private String url_name;

    //页面名称
    private String web_name;

    //所属系统
    private String web_sys;

    //ip所属地
    private String ip_address;

    //慧贸贸组织id
    private String member_id;

    //慧贸贸组织统一社会信用代码
    private String member_uscc;

    //ip所在省/市
    private String ip_province;

    //ip所在市/区
    private String ip_city;

}

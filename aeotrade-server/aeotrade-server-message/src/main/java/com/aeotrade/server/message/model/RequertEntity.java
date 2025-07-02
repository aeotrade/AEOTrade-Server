package com.aeotrade.server.message.model;

import lombok.Data;


/**
 * @Auther: 吴浩
 * @Date: 2022-10-25 13:51
 */
@Data
public class RequertEntity {
    /**
     * 所有请求头
     */
    private org.bson.Document headers;

    /**
     * 被请求URI
     */
    private String requestUri;
    /**
     * 被拦截请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String  param;

    /**
     * body参数
     */
    private String bodyParam;
    /**
     * 拦截时间点
     */
    private Long createTime;

    private String token;
}

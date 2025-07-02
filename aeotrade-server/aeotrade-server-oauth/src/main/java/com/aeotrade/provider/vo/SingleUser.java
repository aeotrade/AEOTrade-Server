package com.aeotrade.provider.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2021-08-19 13:30
 */
@Data
public class SingleUser {
    //重定向网址
    private String service;
    private String id;
    private String client_id;
    //用户信息
    private SingleAttributes attributes;
}

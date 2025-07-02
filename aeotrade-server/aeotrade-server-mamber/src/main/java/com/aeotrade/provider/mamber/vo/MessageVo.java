package com.aeotrade.provider.mamber.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2021/3/22 10:53
 */
@Data
public class MessageVo implements Serializable {
    private String orderId;
    private String goodsCategoryId;
    private String vipclass;
    private String endtime;
    private String userId;
    private String apply;
    private String type;
}

package com.aeotrade.provider.mamber.vo;

import lombok.Data;

/**
 * @Author: yewei
 * @Date: 2020/7/9 15:15
 */
@Data
public class SmsHomeAdvertiseVO {
    private Long id;

    private String name;

    //@ApiModelProperty(value = "轮播位置：0->PC首页轮播；1->app首页轮播")
    private Integer type;

    private String pic;

    private long startTime;

    private long endTime;

    //@ApiModelProperty(value = "上下线状态：0->下线；1->上线")
    private Integer status;

    //@ApiModelProperty(value = "点击数")
    private Integer clickCount;

    //@ApiModelProperty(value = "下单数")
    private Integer orderCount;

    //@ApiModelProperty(value = "链接地址")
    private String url;

    //@ApiModelProperty(value = "备注")
    private String note;

    //@ApiModelProperty(value = "排序")
    private Integer sort;
}

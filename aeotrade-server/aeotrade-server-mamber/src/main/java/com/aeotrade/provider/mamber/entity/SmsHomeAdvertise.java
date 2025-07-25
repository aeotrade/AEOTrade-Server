package com.aeotrade.provider.mamber.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@TableName("sms_home_advertise")
@Data
public class SmsHomeAdvertise implements Serializable {
    private Long id;

    private String name;

    //@ApiModelProperty(value = "轮播位置：0->PC首页轮播；1->app首页轮播")
    private Integer type;

    private String pic;

    private Date startTime;

    private Date endTime;

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
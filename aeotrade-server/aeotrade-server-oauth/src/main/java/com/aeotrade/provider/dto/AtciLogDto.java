package com.aeotrade.provider.dto;

import lombok.Data;

/**
 * 接收日志推送
 * @Author: yewei
 * @Date: 2020/5/8 9:35
 */
@Data
public class AtciLogDto {

    //单证数量
    private Integer billCount;

    //日期
    private String billDate;

    //单证类型,1:报关单,2:关税单,3:退税单,4:核注清单,5:随附单据
    private Integer billType;

    //采集方式
    private String billWay;

    //企业Id
    private Long memberId;

    //操作人
    private String operator;

    //更新时间
    private java.sql.Timestamp updateTime;

    //社会统一信用代码
    private String memberUscc;

}

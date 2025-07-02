package com.aeotrade.provider.mamber.vo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2022-04-26 10:03
 */
@Data
public class MessageUpdate {
    private Long memberId;
    private Long oldVipTypeId;
    private Long newVipTypeId;
    private String newVipClass;

}

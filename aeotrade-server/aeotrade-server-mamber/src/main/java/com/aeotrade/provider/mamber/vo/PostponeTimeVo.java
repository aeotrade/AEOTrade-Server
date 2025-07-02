package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: 吴浩
 * @Date: 2021/5/24 11:06
 * 会员延期vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostponeTimeVo {
    private Long id;
    private Long operatorId;
    private String operator;
    private String operatorTime;
    private Long memberId;
    private String memberName;
    private String classSerialNumber;
    private String mamberName;
    private String vipTypeName;
    private Long vipTypeId;
    private int day;
}

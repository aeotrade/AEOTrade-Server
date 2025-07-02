package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Auther: 吴浩
 * @Date: 2021/4/14 13:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VipClass implements Serializable {
    private Long memberId;
    private Long typeId;
    private Long classId;
}

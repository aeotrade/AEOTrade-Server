package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/7/15 18:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsHomeAdvertiseResut implements Serializable {

    private List<SmsHomeAdvertiseVO > list;
    private int total;

}

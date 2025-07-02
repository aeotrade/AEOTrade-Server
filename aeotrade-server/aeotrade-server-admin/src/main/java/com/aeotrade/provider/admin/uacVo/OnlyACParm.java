package com.aeotrade.provider.admin.uacVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 2020/6/12 15:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlyACParm implements Serializable {
    private  int code;
    private String message;
}

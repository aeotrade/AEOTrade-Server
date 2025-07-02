package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author: yewei
 * @Date: 10:50 2021/3/3
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CloudAppDtoID implements Serializable {

    private Map<String, Integer> ids;

    private  Integer type;

    private String id;
}

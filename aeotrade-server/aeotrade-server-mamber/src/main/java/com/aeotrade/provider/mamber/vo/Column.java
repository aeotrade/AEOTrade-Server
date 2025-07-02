package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yewei
 * @Date: 2020/5/13 11:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Column {
    private Long id;

    //@ApiModelProperty(value="栏目名字", allowEmptyValue=true)
    private String name;

    private Integer columnStatus;


}

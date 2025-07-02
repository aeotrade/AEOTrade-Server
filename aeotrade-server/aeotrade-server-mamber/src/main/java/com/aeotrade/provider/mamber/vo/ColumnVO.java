package com.aeotrade.provider.mamber.vo;
import lombok.Data;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/5/13 11:23
 */
@Data
public class ColumnVO {
    private Long id;

    //@ApiModelProperty(value="栏目名字", allowEmptyValue=true)
    private String name;

    private Integer columnStatus;

    private List<Column>  children;
}

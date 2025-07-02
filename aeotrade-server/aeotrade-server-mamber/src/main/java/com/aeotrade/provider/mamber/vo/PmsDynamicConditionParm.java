package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 2020/6/16 19:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsDynamicConditionParm implements Serializable {
    private String  id;
    /**表名*/
    private String  tableName;
    /**字段名*/
    private String  fieldNames;
    /**排序*/
    private String sort;
    /**
     * {name:"商品",value:1},
     * {name:"贸易商",value:2},
     * {name:"服务商",value:6},
     *{name:"服务商店铺",value:7},
     * {name:"贸易商店铺",value:8},
     * {name:"资讯",value:3},
     * {name:"类目",value:4},
     * {name:"服务",value:5},
     * {name:"应用",value:9},
     */
    private Integer type;
}

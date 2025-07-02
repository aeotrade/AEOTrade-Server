package com.aeotrade.provider.mamber.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签,文章多对多关系表
 * @Author: yewei
 * @Date: 2020/4/1 11:10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxCatCud {
    /**
     * 标签表ID
     */
    private Long catId;
    /**
     * 图文表media_id
     */
    private String ucdId;
}

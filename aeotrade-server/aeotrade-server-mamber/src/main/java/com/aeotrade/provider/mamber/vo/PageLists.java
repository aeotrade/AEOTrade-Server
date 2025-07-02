package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/4/2 17:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageLists<T> {
    private static final long serialVersionUID = 8545996863226528798L;
    private Long PageNum ;
    private Long pageSize;
    private Long totalPage;
    private Long total;
    private List<T> data;

}

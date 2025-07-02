package com.aeotrade.provider.mamber.vo;

import com.aeotrade.provider.mamber.entity.PmsCatalogDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 2020/6/17 17:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsCatalogDetailVO extends PmsCatalogDetail implements Serializable {
    private Integer dataType =0;
    private Integer total = 0;
}

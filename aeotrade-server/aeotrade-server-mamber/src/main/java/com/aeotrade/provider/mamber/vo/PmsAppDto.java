package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.PmsApp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 17:49 2021/3/4
 * @Description:
 */

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsAppDto extends PmsApp implements Serializable {
    private  Integer  form;
}

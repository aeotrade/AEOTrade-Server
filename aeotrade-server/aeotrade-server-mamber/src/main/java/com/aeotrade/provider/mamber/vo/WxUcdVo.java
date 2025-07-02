package com.aeotrade.provider.mamber.vo;

import com.aeotrade.provider.mamber.entity.WxUcd;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/4/7 17:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxUcdVo {
    private WxUcd wxUcd;
    private List<Cid> cid;
}

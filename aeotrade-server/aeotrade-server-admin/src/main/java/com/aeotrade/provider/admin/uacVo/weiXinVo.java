package com.aeotrade.provider.admin.uacVo;

import lombok.Data;

/**
 * @Auther: 吴浩
 * @Date: 2023-12-19 16:12
 */
@Data
public class weiXinVo {
    private Integer errcode;

    private String errmsg;

    private PhoneInfo phone_info;
}

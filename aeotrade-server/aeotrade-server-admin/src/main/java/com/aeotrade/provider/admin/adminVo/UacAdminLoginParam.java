package com.aeotrade.provider.admin.adminVo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

/**
 * 用户登录参数
 * Created by hmm on 2018/4/26.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UacAdminLoginParam {
    @NotEmpty
    //@ApiModelProperty(value = "用户名",required = true)
    private String username;
    @NotEmpty
    //@ApiModelProperty(value = "密码",required = true)
    private String password;

    private String code;
}

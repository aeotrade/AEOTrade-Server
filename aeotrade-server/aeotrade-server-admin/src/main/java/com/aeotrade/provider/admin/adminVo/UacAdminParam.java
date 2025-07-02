package com.aeotrade.provider.admin.adminVo;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * 用户登录参数
 * Created by hmm on 2018/4/26.
 */
@Getter
@Setter
public class UacAdminParam {
    @NotEmpty
    //@ApiModelProperty(value = "用户名", required = true)
    private String username;
    @NotEmpty
    //@ApiModelProperty(value = "密码", required = true)
    private String password;
    //@ApiModelProperty(value = "用户头像")
    private String icon;
    @Email
    //@ApiModelProperty(value = "邮箱")
    private String email;
    //@ApiModelProperty(value = "用户昵称")
    private String nickName;
    //@ApiModelProperty(value = "备注")
    private String note;
    //@ApiModelProperty(value = "手机号")
    private String mobile;

    //@ApiModelProperty(value = "帐号启用状态：0->禁用；1->启用")
    private Integer status;
}

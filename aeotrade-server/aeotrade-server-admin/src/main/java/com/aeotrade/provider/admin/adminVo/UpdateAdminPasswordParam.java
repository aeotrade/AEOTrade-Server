package com.aeotrade.provider.admin.adminVo;

import lombok.Getter;
import lombok.Setter;


/**
 * 修改用户名密码参数
 * hmm on 2019/10/9.
 */
@Getter
@Setter
public class UpdateAdminPasswordParam {
   // @NotEmpty
   // @ApiModelProperty(value = "用户名", required = true)
    private String username;
    //@NotEmpty
    //@ApiModelProperty(value = "旧密码", required = true)
    private String oldPassword;
   // @NotEmpty
   // @ApiModelProperty(value = "新密码", required = true)
    private String newPassword;
}

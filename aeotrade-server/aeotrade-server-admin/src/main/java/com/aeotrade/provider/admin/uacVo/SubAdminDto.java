package com.aeotrade.provider.admin.uacVo;

import lombok.Data;

import javax.validation.constraints.NotNull;

//@ApiModel
@Data
public class SubAdminDto {
    //@ApiModelProperty(value = "当前企业id")
    private Long memberId;
    @NotNull(message = "员工ID 不能为空")
    //@ApiModelProperty(value = "子管理员id")
    private Long staffId;
    private String displayName;
    private String imageUrl;
}

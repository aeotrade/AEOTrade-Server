package com.aeotrade.provider.admin.uacVo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/1/7 20:31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffVO {

    private Long id;

    //@ApiModelProperty(value="部门", allowEmptyValue=true)
    private String dept;

    //@ApiModelProperty(value="客服名称", allowEmptyValue=true)
    private String staffName;

    //@ApiModelProperty(value="可管理企业", allowEmptyValue=true)
    private List memberName;

    //@ApiModelProperty(value="微信授权状态 未授权 0 ;已授权 1", allowEmptyValue=true)
    private Integer sgsStatus;
    //@ApiModelProperty(value="头像", allowEmptyValue=true)
    private String wxLogo;
    //@ApiModelProperty(value="手机号", allowEmptyValue=true)
    private String tel;
    //@ApiModelProperty(value="角色 管理员 :admin 普通员工:user", allowEmptyValue=true)
    private String role;
}

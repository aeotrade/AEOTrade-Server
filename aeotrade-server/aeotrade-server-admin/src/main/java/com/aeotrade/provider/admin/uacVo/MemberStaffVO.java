package com.aeotrade.provider.admin.uacVo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 员工修改回显
 * @Author: yewei
 * @Date: 2020/1/7 23:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberStaffVO {

    //@ApiModelProperty(value="加入企业", allowEmptyValue=true)
    private List<MemberVO> member;

    private Long id ;
    //@ApiModelProperty(value="部门", allowEmptyValue=true)
    private String dept;
    //@ApiModelProperty(value="角色 管理员 :admin 普通员工:user", allowEmptyValue=true)
    private String role;

    //@ApiModelProperty(value="手机号", allowEmptyValue=true)
    private String tel;

    //@ApiModelProperty(value="客服名称", allowEmptyValue=true)
    private String staffName;

    //@ApiModelProperty(value="头像", allowEmptyValue=true)
    private String wxLogo;


}

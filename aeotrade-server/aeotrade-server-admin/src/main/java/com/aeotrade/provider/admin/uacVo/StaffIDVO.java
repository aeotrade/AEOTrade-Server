package com.aeotrade.provider.admin.uacVo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通讯录查找
 * @Author: yewei
 * @Date: 2020/1/8 0:22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffIDVO {
    private Long id;

    //@ApiModelProperty(value="部门", allowEmptyValue=true)
    private String dept;

    //@ApiModelProperty(value="客服名称", allowEmptyValue=true)
    private String staffName;
}

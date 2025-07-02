package com.aeotrade.provider.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: 吴浩
 * @Date: 2023-12-06 10:15
 */
@Getter
@Setter
@TableName("uac_dept_staff")
public class UacDeptStaff {

    private Long id;

    private Long deptId;

    private Long staffId;
}
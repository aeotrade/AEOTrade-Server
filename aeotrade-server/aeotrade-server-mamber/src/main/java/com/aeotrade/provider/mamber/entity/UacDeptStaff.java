package com.aeotrade.provider.mamber.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-30 10:43
 */
@Getter
@Setter
@TableName("uac_dept_staff")
public class UacDeptStaff {

    private Long id;

    private Long deptId;

    private Long staffId;
}

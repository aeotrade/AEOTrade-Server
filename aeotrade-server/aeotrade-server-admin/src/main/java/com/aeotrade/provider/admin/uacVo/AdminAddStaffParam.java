package com.aeotrade.provider.admin.uacVo;

import lombok.Data;

@Data
public class AdminAddStaffParam {
    private String memberId;
    private String staffId;
    private String mobile;
    private String name;
    private String[] roleId;
    private String[] deptId;
}

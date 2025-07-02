package com.aeotrade.provider.admin.adminVo;

import com.aeotrade.provider.admin.entiy.UacRoleDocment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2022-01-06 20:24
 */
@Getter
@Setter
public class RoleMenu {
    private String roleId;
    private Long organ;
    private List<String> menuIds;
    private List<UacRoleDocment> uacRoleDocments;
}

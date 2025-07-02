package com.aeotrade.provider.admin.adminVo;

import com.aeotrade.provider.admin.entiy.UacRoleDocment;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import lombok.Data;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2022-11-03 14:08
 */
@Data
public class RoleMenuDocment {
    private List<UawWorkbenchMenu> uawWorkbenchMenus;
    private List<UacRoleDocment> uacRoleDocments;
}

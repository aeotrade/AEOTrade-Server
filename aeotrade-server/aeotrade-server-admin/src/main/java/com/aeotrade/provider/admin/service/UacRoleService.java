package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.adminVo.RoleMenuDocment;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.aeotrade.provider.admin.entiy.UacRoleDocment;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * <p>
 * 后台用户角色表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
public interface UacRoleService extends MPJBaseService<UacRole> {

    List<UawWorkbenchMenu> getMenuList(Long id);

    List<UacRole> listAll(List<Long> roleIds);

    RoleMenuDocment listMenu(Long roleId);

    int allocMenu(String roleId, List<String> menuIds, List<UacRoleDocment> uacRoleDocments);

    Object memberRole(Long platformId, Long memberId, Long organ);

    void RoleRedis(Long platformId);

    void insertRoleRedis(Long id);

    int findRole(Long memberId, Long workbenchId);

    List<Long> findMemberAllList();

    List<Long> findMemberAll(Long platformId);

    List<Long> findStaffIds(Long roleId,Long memberId);
}

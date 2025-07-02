package com.aeotrade.provider.admin.service;

import com.aeotrade.provider.admin.adminVo.UacMenuNode;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-10 14:47
 */
public interface UawWorkbenchMenuService extends MPJBaseService<UawWorkbenchMenu> {
    List<UacMenuNode> ListMenuUser(Long id);

    UawWorkbenchMenu getMenu(Long parentId, Integer sort);

    int create(UawWorkbenchMenu uacMenu);

    int updateMenu(Long id, UawWorkbenchMenu uacMenu);

    List<UawWorkbenchMenu> findlist(Long parentId, Integer pageSize, Integer pageNum);

    List<UacMenuNode> treeList();

    int updateHidden(Long id, Integer hidden);

    List<UawWorkbenchMenu> findMenuByRoleId(Long roleId);
}

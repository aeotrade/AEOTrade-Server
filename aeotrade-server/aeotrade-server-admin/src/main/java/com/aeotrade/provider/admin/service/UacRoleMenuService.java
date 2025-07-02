package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.entiy.UacRoleMenu;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
public interface UacRoleMenuService extends MPJBaseService<UacRoleMenu> {

    List<Long> findWorkBenchMenu(String s);
}

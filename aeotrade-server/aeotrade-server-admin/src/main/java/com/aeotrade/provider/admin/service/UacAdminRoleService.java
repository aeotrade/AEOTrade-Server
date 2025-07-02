package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.entiy.UacAdminRole;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * <p>
 * 后台用户和角色关系表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
public interface UacAdminRoleService extends MPJBaseService<UacAdminRole> {

    List<UacRole> findRole(Long id);

}

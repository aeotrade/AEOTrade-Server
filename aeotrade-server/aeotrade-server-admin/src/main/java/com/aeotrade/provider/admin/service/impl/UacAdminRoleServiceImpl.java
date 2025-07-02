package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.entiy.UacAdmin;
import com.aeotrade.provider.admin.entiy.UacAdminRole;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.aeotrade.provider.admin.mapper.UacAdminRoleMapper;
import com.aeotrade.provider.admin.service.UacAdminRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 后台用户和角色关系表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Service
public class UacAdminRoleServiceImpl extends ServiceImpl<UacAdminRoleMapper, UacAdminRole> implements UacAdminRoleService {
    @Autowired
    private UacRoleServiceImpl uacRoleService;

    @Override
    public List<UacRole> findRole(Long id) {
        return uacRoleService.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                .selectAll(UacRole.class)
                .innerJoin(UacAdminRole.class,UacAdminRole::getRoleId,UacRole::getId)
                .innerJoin(UacAdmin.class,UacAdmin::getId,UacAdminRole::getAdminId)
                .eq(UacAdmin::getId,id)
                .eq(UacAdminRole::getMemberId,0)
                );
    }

}

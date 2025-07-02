package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.entiy.UacRoleMenu;
import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import com.aeotrade.provider.admin.mapper.UacRoleMenuMapper;
import com.aeotrade.provider.admin.service.UacRoleMenuService;
import com.aeotrade.provider.admin.service.UawWorkbenchMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Service
public class UacRoleMenuServiceImpl extends ServiceImpl<UacRoleMenuMapper, UacRoleMenu> implements UacRoleMenuService {
    @Autowired
    private UawWorkbenchMenuService uawWorkbenchMenuService;
    @Override
    public List<Long> findWorkBenchMenu(String s) {
        return uawWorkbenchMenuService.lambdaQuery().eq(UawWorkbenchMenu::getWorkbenchId,s).list().stream()
                .map(UawWorkbenchMenu::getId).collect(Collectors.toList());
    }
}

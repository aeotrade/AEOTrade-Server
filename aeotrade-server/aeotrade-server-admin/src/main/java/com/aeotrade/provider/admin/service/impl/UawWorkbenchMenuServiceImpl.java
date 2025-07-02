package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.adminVo.MenuMetaDto;
import com.aeotrade.provider.admin.adminVo.UacMenuNode;

import com.aeotrade.provider.admin.entiy.*;

import com.aeotrade.provider.admin.mapper.UawWorkbenchMenuMapper;
import com.aeotrade.provider.admin.service.UacRoleMenuService;
import com.aeotrade.provider.admin.service.UawWorkbenchMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-10 14:47
 */
@Service
public class UawWorkbenchMenuServiceImpl extends ServiceImpl<UawWorkbenchMenuMapper, UawWorkbenchMenu> implements UawWorkbenchMenuService {
    @Autowired
    private UacRoleMenuService uacRoleMenuService;
    @Override
    public List<UacMenuNode> ListMenuUser(Long id) {
        List<Long> longs = uacRoleMenuService.selectJoinList(UacRoleMenu.class,
                MPJWrappers.<UacRoleMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRoleMenu.class)
                        .leftJoin(UacRole.class, UacRole::getId, UacRoleMenu::getRoleId)
                        .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                        .eq(UacAdmin::getId, id)
                        .eq(UacAdminRole::getMemberId, 0)
                        .eq(UacRole::getPlatform, 2)
                        .ne(UacRole::getStatus, 0)
        ).stream().map(UacRoleMenu::getMenuId).collect(Collectors.toList());
        List<UawWorkbenchMenu> uacMenuEntities = this.lambdaQuery()
                .in(UawWorkbenchMenu::getId, longs)
                .eq(UawWorkbenchMenu::getIsHidden,0)
                .eq(UawWorkbenchMenu::getStatus,0)
                .orderByAsc(UawWorkbenchMenu::getSort).list();
        for (UawWorkbenchMenu uacMenuEntity : uacMenuEntities) {
            MenuMetaDto menuMetaDto = new MenuMetaDto();
            if (null != uacMenuEntity.getTitle()) {
                menuMetaDto.setTitle(uacMenuEntity.getTitle());
            }
            if (null != uacMenuEntity.getIcon()) {
                menuMetaDto.setIcon(uacMenuEntity.getIcon());
            }
            if (null != uacMenuEntity.getIframeUrl()) {
                menuMetaDto.setIframeUrl(uacMenuEntity.getIframeUrl());
            }
            uacMenuEntity.setMeta(menuMetaDto);
        }
        if (uacMenuEntities.size() != 0) {
            List<UawWorkbenchMenu> menuList = uacMenuEntities.stream().distinct().collect(Collectors.toList());
            List<UacMenuNode> result = menuList.stream()
                    .filter(menu -> menu.getParentId().equals(0L))
                    .map(menu -> covertMenuNode(menu, menuList)).collect(Collectors.toList());
            return result;
        }
        return null;
    }

    @Override
    public UawWorkbenchMenu getMenu(Long parentId, Integer sort) {
        List<UawWorkbenchMenu> list = this.lambdaQuery().eq(UawWorkbenchMenu::getParentId, parentId).eq(UawWorkbenchMenu::getSort, sort).list();
        return list.size()>0?list.get(0):null;

    }

    @Override
    public int create(UawWorkbenchMenu uacMenu) {
        uacMenu.setCreateTime(LocalDateTime.now());
        updateLevel(uacMenu);
        this.save(uacMenu);
        return 1;
    }

    @Override
    public int updateMenu(Long id, UawWorkbenchMenu uacMenu) {
        uacMenu.setId(id);
        updateLevel(uacMenu);
        this.updateById(uacMenu);
        return 1;
    }

    @Override
    public List<UawWorkbenchMenu> findlist(Long parentId, Integer pageSize, Integer pageNum) {
        return this.lambdaQuery().eq(UawWorkbenchMenu::getParentId, parentId)
                .eq(UawWorkbenchMenu::getType, 2)
                .orderByDesc(UawWorkbenchMenu::getSort).list();
    }

    @Override
    public List<UacMenuNode> treeList() {
        List<UawWorkbenchMenu> menuList = this.lambdaQuery().eq(UawWorkbenchMenu::getType, 2).list();
        List<UacMenuNode> result = menuList.stream()
                .filter(menu -> menu.getParentId().equals(0L))
                .map(menu -> covertMenuNode(menu, menuList)).collect(Collectors.toList());
        return result;
    }

    @Override
    public int updateHidden(Long id, Integer hidden) {
        UawWorkbenchMenu UacMenu = new UawWorkbenchMenu();
        UacMenu.setId(id);
        UacMenu.setHidden(hidden);
        this.updateById(UacMenu);
        return 1;
    }

    @Override
    public List<UawWorkbenchMenu> findMenuByRoleId(Long roleId) {
        List<Long> collect = uacRoleMenuService.lambdaQuery()
                .eq(UacRoleMenu::getRoleId, roleId).list().stream().map(UacRoleMenu::getMenuId).collect(Collectors.toList());
        if(collect.size()!=0){
            return this.lambdaQuery().in(UawWorkbenchMenu::getId,collect)
                    .orderByAsc(UawWorkbenchMenu::getSort).list();
        }
        return new ArrayList<>();
    }

    /**
     * 修改菜单层级
     */
    private void updateLevel(UawWorkbenchMenu UacMenu) {
        if (UacMenu.getParentId() == 0) {
            //没有父菜单时为一级菜单
            UacMenu.setLevel(0);
        } else {
            //有父菜单时选择根据父菜单level设置
            List<UawWorkbenchMenu> list = this.lambdaQuery().eq(UawWorkbenchMenu::getParentId, UacMenu.getParentId()).list();
            UawWorkbenchMenu parentMenu =list.size()>0?list.get(0):null;
            if (parentMenu != null) {
                UacMenu.setLevel(parentMenu.getLevel() + 1);
            } else {
                UacMenu.setLevel(0);
            }
        }
    }

    /**
     * 将UacMenu转化为UacMenuNode并设置children属性
     */
    private UacMenuNode covertMenuNode(UawWorkbenchMenu menu, List<UawWorkbenchMenu> menuList) {
        UacMenuNode node = new UacMenuNode();
        BeanUtils.copyProperties(menu, node);
        List<UacMenuNode> children = menuList.stream()
                .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
                .map(subMenu -> covertMenuNode(subMenu, menuList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }
}

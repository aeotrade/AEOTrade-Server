package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.adminVo.RoleMenuDocment;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.service.*;
import com.aeotrade.provider.admin.mapper.UacRoleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 后台用户角色表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Service
public class UacRoleServiceImpl extends ServiceImpl<UacRoleMapper, UacRole> implements UacRoleService {
    @Autowired
    private UawWorkbenchMenuService uawWorkbenchMenuService;
    @Autowired
    private UacRoleDocmentService uacRoleDocmentService;
    @Autowired
    private UacRoleMenuService uacRoleMenuService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UawVipMessageService uawVipMessageService;
    @Autowired
    private UawVipTypeService uawVipTypeService;

    @Autowired
    private UacAdminService uacAdminService;


    @Override
    public List<UawWorkbenchMenu> getMenuList(Long id) {
        List<UawWorkbenchMenu> uawWorkbenchMenus = new ArrayList<>();
        List<UacRole> roleList = this.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRole.class)
                        .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .eq(UacAdminRole::getAdminId, id)
                        .eq(UacAdminRole::getMemberId, 0)
                        .eq(UacRole::getPlatform, 2)
                        .eq(UacRole::getStatus, 1)
        );
        for (UacRole uacRole : roleList) {
            List<UawWorkbenchMenu> menuByroleId = uawWorkbenchMenuService.selectJoinList(UawWorkbenchMenu.class,
                    MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawWorkbenchMenu.class)
                            .leftJoin(UacRoleMenu.class, UacRoleMenu::getMenuId, UawWorkbenchMenu::getId)
                            .eq(UacRoleMenu::getRoleId, uacRole.getId())
                            .eq(UawWorkbenchMenu::getType, 1)
            );
            uawWorkbenchMenus.contains(menuByroleId);
        }
        return uawWorkbenchMenus;
    }

    @Override
    public List<UacRole> listAll(List<Long> roleIds) {
        return this.lambdaQuery().in(UacRole::getId, roleIds).list();
    }

    @Override
    public RoleMenuDocment listMenu(Long roleId) {
        RoleMenuDocment roleMenuDocment = new RoleMenuDocment();
        List<UawWorkbenchMenu> menuByRoleId = uawWorkbenchMenuService.findMenuByRoleId(roleId);
        if (null != menuByRoleId) {
            roleMenuDocment.setUawWorkbenchMenus(menuByRoleId);
        }
        UacRoleDocment uacRoleDocment = new UacRoleDocment();
        uacRoleDocment.setRoleId(roleId);
        List<UacRoleDocment> roleDocments = uacRoleDocmentService.lambdaQuery(uacRoleDocment).list();

        if (null != roleDocments && roleDocments.size() != 0) {
            roleMenuDocment.setUacRoleDocments(roleDocments);
        }

        return roleMenuDocment;
    }

    @Override
    public int allocMenu(String roleId, List<String> menuIds, List<UacRoleDocment> uacRoleDocments) {
        //先删除原有关系
        UacRoleMenu uacRoleMenu = new UacRoleMenu();
        uacRoleMenu.setRoleId(Long.valueOf(roleId));
        List<UacRoleMenu> list = uacRoleMenuService.lambdaQuery(uacRoleMenu).list();
        for (UacRoleMenu roleModelMenu : list) {
            uacRoleMenuService.removeById(roleModelMenu);
        }
        UacRoleDocment uacRoleDocment = new UacRoleDocment();
        uacRoleDocment.setRoleId(Long.valueOf(roleId));
        List<UacRoleDocment> roleDocments = uacRoleDocmentService.lambdaQuery(uacRoleDocment).list();
        for (UacRoleDocment uacroleDocment : roleDocments) {
            uacRoleDocmentService.removeById(uacroleDocment);
        }
        for (String aLong : menuIds) {
            UacRoleMenu uacRoleMen = new UacRoleMenu();
            uacRoleMen.setRoleId(Long.valueOf(roleId));
            uacRoleMen.setMenuId(Long.valueOf(aLong));
            uacRoleMenuService.save(uacRoleMen);
        }
        if (null != uacRoleDocments) {
            for (UacRoleDocment roleDocment : uacRoleDocments) {
                roleDocment.setRoleId(Long.valueOf(roleId));
                uacRoleDocmentService.save(roleDocment);
            }
        }
        UacRole uacRole = this.getById(Long.valueOf(roleId));
        insertRoleRedis(Long.valueOf(roleId));
        return menuIds.size();
    }

    @Override
    public Object memberRole(Long platformId, Long memberId, Long organ) {
        UacRole uacRole = new UacRole();
        uacRole.setPlatformId(platformId);
        uacRole.setOrgid(String.valueOf(memberId));
        uacRole.setIsModel(0);
        if (null != organ) {
            uacRole.setOrgan(String.valueOf(organ));
        }
        List<UacRole> list = this.lambdaQuery(uacRole).list();
        return list;
    }

    @Override
    public void RoleRedis(Long platformId) {
        UacRole uacRole=new UacRole();
        uacRole.setPlatformId(platformId);
        List<Long> list = this.lambdaQuery(uacRole).list().stream().map(UacRole::getId).collect(Collectors.toList());
        for (Long role : list) {
            List<UacRoleMenu> collect = uacRoleMenuService.lambdaQuery()
                    .eq(UacRoleMenu::getRoleId, role).list();
            if(collect.size()!=0){
                List<UawWorkbenchMenu> menus = uawWorkbenchMenuService.lambdaQuery()
                        .in(UawWorkbenchMenu::getId,collect.stream()
                                .map(UacRoleMenu::getMenuId).collect(Collectors.toList()))
                        .eq(UawWorkbenchMenu::getType,2).list();
                if (menus.size() != 0) {
                    stringRedisTemplate.opsForValue().set("AEOTRADE_MENU:" + role, StringUtils.join(menus.stream()
                            .map(UawWorkbenchMenu::getButtonPath).toArray(), ","));
                }
            }
            List<UacRoleDocment> docmentNameEn = uacRoleDocmentService.lambdaQuery()
                    .eq(UacRoleDocment::getIsLook,1)
                    .eq(UacRoleDocment::getRoleId,role).list();
            if (docmentNameEn.size() != 0) {
                stringRedisTemplate.opsForValue().set("AEOTRADE_DOCMENT:" + role, StringUtils.join(docmentNameEn.stream()
                        .map(UacRoleDocment::getDocumentTypeNameEn).toArray(), ","));
            }
        }
    }


    public void insertRoleRedis(Long aLong) {
        List<Long> collect = uacRoleMenuService.lambdaQuery()
                .eq(UacRoleMenu::getRoleId, aLong)
                .list().stream().map(UacRoleMenu::getMenuId).collect(Collectors.toList());
        List<UawWorkbenchMenu> menus = uawWorkbenchMenuService.lambdaQuery()
                .eq(UawWorkbenchMenu::getType,2)
                .in(UawWorkbenchMenu::getId,collect).list();
        if (menus.size() != 0) {
            stringRedisTemplate.opsForValue().set("AEOTRADE_MENU:" + aLong, StringUtils.join(menus.stream()
                    .map(UawWorkbenchMenu::getButtonPath).toArray(), ","));
        }
        List<UacRoleDocment> docmentNameEn = uacRoleDocmentService.lambdaQuery()
                .eq(UacRoleDocment::getIsLook,1).eq(UacRoleDocment::getRoleId,aLong).list();
        if (docmentNameEn.size() != 0) {
            stringRedisTemplate.opsForValue().set("AEOTRADE_DOCMENT:" + aLong, StringUtils.join(docmentNameEn.stream()
                    .map(UacRoleDocment::getDocumentTypeNameEn).toArray(), ","));
        }
    }

    @Override
    public int findRole(Long memberId, Long workbenchId) {
        return Math.toIntExact(this.lambdaQuery()
                .eq(UacRole::getOrgid, memberId)
                .eq(UacRole::getOrgan, 100)
                .eq(UacRole::getPlatform, 3)
                .eq(UacRole::getPlatformId, workbenchId).count());
//        return 0;
    }

    @Override
    public List<Long> findMemberAllList() {
        return uacMemberService.lambdaQuery().eq(UacMember::getStatus,0)
                .eq(UacMember::getKindId,1).list().stream()
                .map(UacMember::getId).collect(Collectors.toList());
    }

    @Override
    public List<Long> findMemberAll(Long platformId) {
        List<Long> longs = uawVipTypeService.lambdaQuery()
                .eq(UawVipType::getWorkbench, platformId)
                .eq(UawVipType::getStatus, 0)
                .list().stream().map(UawVipType::getId)
                .collect(Collectors.toList());
      return uawVipMessageService.lambdaQuery()
                .in(UawVipMessage::getTypeId,longs)
                .eq(UawVipMessage::getUserType,1)
                .eq(UawVipMessage::getStatus,0).list()
                .stream().map(UawVipMessage::getMemberId).collect(Collectors.toList());
    }



    @Override
    public List<Long> findStaffIds(Long roleId,Long memberId) {
        List<UacAdmin> uacAdmins = uacAdminService.selectJoinList(UacAdmin.class,
                MPJWrappers.<UacAdmin>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacAdmin.class)
                        .leftJoin(UacAdminRole.class,UacAdminRole::getAdminId,UacAdmin::getId)
                        .leftJoin(UacRole.class, UacRole::getId, UacAdminRole::getRoleId)
                        .eq(UacRole::getId, roleId)
                        .eq(UacAdminRole::getMemberId,memberId)
                        .eq(UacAdmin::getStatus, 1)
        );
        if(uacAdmins.size()==0){
            return new ArrayList<>();
        }
        return uacAdmins.stream().map(UacAdmin::getStaffId).collect(Collectors.toList());
    }

}

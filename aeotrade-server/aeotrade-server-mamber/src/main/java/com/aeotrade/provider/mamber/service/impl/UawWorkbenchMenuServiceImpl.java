package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.mapper.UawWorkbenchMenuMapper;
import com.aeotrade.provider.mamber.service.UawWorkbenchMenuService;
import com.aeotrade.provider.mamber.vo.MenuMetaDto;
import com.aeotrade.provider.mamber.vo.UawWorkbenchMenuVo;
import com.aeotrade.provider.mamber.vo.WorkbenchVo;
import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.aeotrade.utlis.HttpRequestUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawWorkbenchMenuServiceImpl extends ServiceImpl<UawWorkbenchMenuMapper, UawWorkbenchMenu> implements UawWorkbenchMenuService {
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeService;
    @Autowired
    private UawVipClassServiceImpl uawVipClassService;
    @Autowired
    private UawVipClassMenuServiceImpl uawVipClassMenuMapper;
    @Autowired
    private UacAdminServiceImpl uacAdminService;
    @Autowired
    private UacAdminRoleServiceImpl uacAdminRoleService;
    @Autowired
    private UacRoleServiceImpl uacRoleService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UacRoleMenuServiceImpl uacRoleMenuService;

    private static WorkbenchVo toCommentVO(UawWorkbenchMenu comments) {
        WorkbenchVo rvo = new WorkbenchVo();
        BeanUtils.copyProperties(comments, rvo);
        return rvo;
    }

    public List<WorkbenchVo> findbyWorkbenchId(Long id, int type) {
        List<UawWorkbenchMenu> list = this.lambdaQuery().eq(UawWorkbenchMenu::getStatus, 0)
                .eq(UawWorkbenchMenu::getWorkbenchId, id)
                .eq(UawWorkbenchMenu::getPlatformType, type).list();
        List<UawWorkbenchMenuVo> uawWorkbenchMenuVos = new ArrayList<>();
        for (UawWorkbenchMenu uacMenuEntity : list) {
            UawWorkbenchMenuVo uawWorkbenchMenuVo = new UawWorkbenchMenuVo();
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
            BeanUtils.copyProperties(uacMenuEntity, uawWorkbenchMenuVo);
            uawWorkbenchMenuVo.setMeta(menuMetaDto);
            uawWorkbenchMenuVos.add(uawWorkbenchMenuVo);

        }
        List<WorkbenchVo> workbenchVoList = new ArrayList<>();
        if (!CommonUtil.isEmpty(list)) {
            workbenchVoList = splitWorks(0L, uawWorkbenchMenuVos);
        }
        Collections.sort(workbenchVoList);
        return workbenchVoList;
    }

    public List<WorkbenchVo> findbyDefault(Long id, int type, Long memberId) {
        List<UawWorkbenchMenu> list = null;
        if (memberId == 0) {
            list = this.lambdaQuery().eq(UawWorkbenchMenu::getStatus, 0)
                    .eq(UawWorkbenchMenu::getWorkbenchId, id)
                    .eq(UawWorkbenchMenu::getIsHidden, 0)
                    .eq(UawWorkbenchMenu::getPlatformType, type).list();
        } else {
            list = this.selectJoinList(UawWorkbenchMenu.class,
                    MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawWorkbenchMenu.class)
                            .leftJoin(UawVipClassMenu.class, UawVipClassMenu::getMenuId, UawWorkbenchMenu::getId)
                            .leftJoin(UawVipClass.class, UawVipClass::getId, UawVipClassMenu::getClassId)
                            .leftJoin(UawVipMessage.class, UawVipMessage::getClassSerialNumber, UawVipClass::getClassSerialNumber)
                            .eq(UawVipMessage::getMemberId, memberId)
                            .eq(UawVipMessage::getStatus, 0)
                            .eq(UawWorkbenchMenu::getWorkbenchId, id)
                            .eq(UawWorkbenchMenu::getStatus, 0)
                            .eq(UawWorkbenchMenu::getIsHidden, 0)
                            .eq(UawWorkbenchMenu::getPlatformType, type)
            );
        }
        List<UawWorkbenchMenuVo> uawWorkbenchMenuVos = new ArrayList<>();
        for (UawWorkbenchMenu uacMenuEntity : list) {
            UawWorkbenchMenuVo uawWorkbenchMenuVo = new UawWorkbenchMenuVo();
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
            BeanUtils.copyProperties(uacMenuEntity, uawWorkbenchMenuVo);
            uawWorkbenchMenuVo.setMeta(menuMetaDto);
            uawWorkbenchMenuVos.add(uawWorkbenchMenuVo);
        }
        List<WorkbenchVo> workbenchVoList = new ArrayList<>();
        if (!CommonUtil.isEmpty(list)) {
            workbenchVoList = splitDefaultWorks(0L, uawWorkbenchMenuVos);
        }
        Collections.sort(workbenchVoList);

        return workbenchVoList;
    }

    private List<WorkbenchVo> splitDefaultWorks(Long parentId, List<UawWorkbenchMenuVo> list) {
        List<WorkbenchVo> voList = new ArrayList<>();
        voList = list.stream().filter(predicate -> predicate.getParentId().equals(parentId))
                .map(UawWorkbenchMenuServiceImpl::toCommentVO).collect(Collectors.toList());
        splitDefaultWorks(voList, list);
        Collections.sort(voList);
        return voList;
    }

    private void splitDefaultWorks(List<WorkbenchVo> workbenchVoList, List<UawWorkbenchMenuVo> list) {
        workbenchVoList.forEach(commentsVo -> {
            List<WorkbenchVo> replayList = splitDefaultWorks(commentsVo.getId(), list);
            List<WorkbenchVo> workbenchVos = new ArrayList<>();
            List<WorkbenchVo> workbenchVosList = new ArrayList<>();
            for (WorkbenchVo workbenchVo : replayList) {
                if (workbenchVo.getType() == 2) {
                    workbenchVos.add(workbenchVo);
                } else {
                    workbenchVosList.add(workbenchVo);
                }
            }
            commentsVo.setChildren(workbenchVosList);
            commentsVo.setButton(workbenchVos);
        });
    }

    public List<WorkbenchVo> findbyMenu(Long id, Long memberId, Long workbenchId) throws Exception {
        List<UawWorkbenchMenu> list = new ArrayList<>();
        if (null == memberId) {
            //如果企业id为空查询个人菜单
            list = uawVipMessageService.findByStaff(id);
        } else {
            //查询企业所有可以使用的菜单
            List<UawWorkbenchMenu> byMember = this.selectJoinList(UawWorkbenchMenu.class,
                    MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawWorkbenchMenu.class)
                            .leftJoin(UawVipClassMenu.class, UawVipClassMenu::getMenuId, UawWorkbenchMenu::getId)
                            .leftJoin(UawVipClass.class, UawVipClass::getId, UawVipClassMenu::getClassId)
                            .leftJoin(UawVipMessage.class, UawVipMessage::getClassSerialNumber, UawVipClass::getClassSerialNumber)
                            .eq(UawWorkbenchMenu::getWorkbenchId, workbenchId)
                            .eq(UawWorkbenchMenu::getStatus, 0)
                            .eq(UawWorkbenchMenu::getIsHidden, 0)
                            .eq(UawWorkbenchMenu::getType, 1)
                            .eq(UawVipMessage::getVipStatus, 1)
                            .eq(UawVipMessage::getStatus, 0)
                            .eq(UawVipMessage::getMemberId, memberId)
            );
            //判断该个人是否为该企业的主管理员
            UacMember admin = uawVipMessageService.findAdmin(id, memberId);
            if (null != admin) {
                //是主管理员查询该企业全部可用菜单
                list = byMember;
                stringRedisTemplate.opsForValue().set("AEOTRADE_ROLE:" + id + memberId, String.valueOf(memberId));
                List<String> menu = this.selectJoinList(UawWorkbenchMenu.class,
                        MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                .selectAll(UawWorkbenchMenu.class)
                                .leftJoin(UawVipClassMenu.class, UawVipClassMenu::getMenuId, UawWorkbenchMenu::getId)
                                .leftJoin(UawVipClass.class, UawVipClass::getId, UawVipClassMenu::getClassId)
                                .leftJoin(UawVipMessage.class, UawVipMessage::getClassSerialNumber, UawVipClass::getClassSerialNumber)
                                .eq(UawWorkbenchMenu::getStatus, 0)
                                .eq(UawWorkbenchMenu::getIsHidden, 0)
                                .eq(UawWorkbenchMenu::getType, 2)
                                .eq(UawVipMessage::getVipStatus, 1)
                                .eq(UawVipMessage::getStatus, 0)
                                .eq(UawVipMessage::getMemberId, memberId)).stream().map(UawWorkbenchMenu::getButtonPath).collect(Collectors.toList());
                stringRedisTemplate.opsForValue().set("AEOTRADE_MENU:" + memberId, StringUtils.join(menu.toArray(), ","));
            } else {
                //不是主管理员查询用户角色
                List<Long> role = uacAdminRoleService.selectJoinList(UacAdminRole.class,
                        MPJWrappers.<UacAdminRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                .selectAll(UacAdminRole.class)
                                .leftJoin(UacRole.class, UacRole::getId, UacAdminRole::getRoleId)
                                .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                                .eq(UacRole::getPlatformId, workbenchId)
                                .eq(UacAdminRole::getMemberId, memberId)
                                .eq(UacRole::getStatus, 1)
                                .eq(UacAdmin::getStaffId, id)
                                .eq(UacAdmin::getIsTab, 1)

                ).stream().map(UacAdminRole::getRoleId).collect(Collectors.toList());
                if (role.size() == 0 && workbenchId != 930060116919582720L) {
                    List<Long> menberRole = uacRoleService.lambdaQuery()
                            .eq(UacRole::getOrgid, memberId)
                            .eq(UacRole::getIsDefault, 1)
                            .eq(UacRole::getPlatformId, workbenchId)
                            .eq(UacRole::getIsModel, 0)
                            .eq(UacRole::getStatus, 1)
                            .list().stream()
                            .map(UacRole::getId).collect(Collectors.toList());
                    if(menberRole.size()==0){
                        menberRole = uacRoleService.lambdaQuery()
                                .eq(UacRole::getOrgid, 0L)
                                .eq(UacRole::getIsDefault, 1)
                                .eq(UacRole::getPlatformId, workbenchId)
                                .eq(UacRole::getIsModel, 1)
                                .eq(UacRole::getStatus, 1)
                                .list().stream()
                                .map(UacRole::getId).collect(Collectors.toList());
                    }
                    if (menberRole.size() != 0) {
                        List<UacAdmin> uacAdmins = uacAdminService.lambdaQuery().eq(UacAdmin::getStaffId, id).list();
                        Long adminId = uacAdmins.size() > 0 ? uacAdmins.get(0).getId() : null;
                        for (Long aLong : menberRole) {
                            UacAdminRole uacAdminRole=new UacAdminRole();
                            uacAdminRole.setRoleId(aLong);
                            uacAdminRole.setAdminId(adminId);
                            uacAdminRole.setMemberId(memberId);
                            uacAdminRole.setOrgi(String.valueOf(workbenchId));
                            uacAdminRoleService.save(uacAdminRole);
                            stringRedisTemplate.opsForValue().set("AEOTRADE_ROLE:" + id + memberId, String.valueOf(aLong));
                        }
                    }
                }
                List<Long> uacRoleMenus = uacRoleMenuService.selectJoinList(UacRoleMenu.class,
                        MPJWrappers.<UacRoleMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                                .selectAll(UacRoleMenu.class)
                                .leftJoin(UacRole.class, UacRole::getId, UacRoleMenu::getRoleId)
                                .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                                .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                                .eq(UacAdminRole::getMemberId, memberId)
                                .eq(UacAdmin::getStaffId, id)
                                .eq(UacRole::getStatus, 1)
                ).stream().map(UacRoleMenu::getMenuId).collect(Collectors.toList());
                List<UawWorkbenchMenu> bystaff=null;
                if(uacRoleMenus.size()!=0){
                    List<UawVipMessage> uawVipMessages = uawVipMessageService.lambdaQuery().eq(UawVipMessage::getMemberId, memberId)
                            .eq(UawVipMessage::getStatus, 0)
                            .eq(UawVipMessage::getUserType, 1)
                            .eq(UawVipMessage::getVipStatus, 1).list();
                    if(uawVipMessages.size()!=0){
                        bystaff = this.lambdaQuery()
                                .in(UawWorkbenchMenu::getId, uacRoleMenus)
                                .eq(UawWorkbenchMenu::getWorkbenchId, workbenchId)
                                .eq(UawWorkbenchMenu::getStatus, 0)
                                .eq(UawWorkbenchMenu::getIsHidden, 0)
                                .eq(UawWorkbenchMenu::getType, 1).list();
                    }
                }
                if (null != bystaff) {
                    for (UawWorkbenchMenu uawWorkbenchMenu : bystaff) {
                        if (byMember.contains(uawWorkbenchMenu)) {
                            list.add(uawWorkbenchMenu);
                        }
                    }
                }
            }
        }
        if (list.size() == 0) {
            //查询默认使用的菜单
            list = this.lambdaQuery().eq(UawWorkbenchMenu::getIsDefault, 1)
                    .eq(UawWorkbenchMenu::getStatus, 0)
                    .eq(UawWorkbenchMenu::getWorkbenchId, workbenchId)
                    .eq(UawWorkbenchMenu::getIsHidden, 0)
                    .eq(UawWorkbenchMenu::getType, 1).list();
        }
        List<UawWorkbenchMenu> collect = list.stream().distinct().collect(Collectors.toList());
        List<UawWorkbenchMenuVo> uawWorkbenchMenuVos = new ArrayList<>();
        for (UawWorkbenchMenu uacMenuEntity : collect) {
            UawWorkbenchMenuVo uawWorkbenchMenuVo = new UawWorkbenchMenuVo();
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
            BeanUtils.copyProperties(uacMenuEntity, uawWorkbenchMenuVo);
            uawWorkbenchMenuVo.setMeta(menuMetaDto);
            uawWorkbenchMenuVos.add(uawWorkbenchMenuVo);
        }
        List<WorkbenchVo> workbenchVoList = new ArrayList<>();
        if (!CommonUtil.isEmpty(uawWorkbenchMenuVos)) {
            workbenchVoList = splitWorks(0L, uawWorkbenchMenuVos);
        }
        Collections.sort(workbenchVoList);
        return workbenchVoList;
    }

    private List<WorkbenchVo> splitWorks(Long parentId, List<UawWorkbenchMenuVo> list) {
        List<WorkbenchVo> voList = new ArrayList<>();
        voList = list.stream().filter(predicate -> predicate.getParentId().equals(parentId))
                .map(UawWorkbenchMenuServiceImpl::toCommentVO).collect(Collectors.toList());
        splitWorks(voList, list);
        Collections.sort(voList);
        return voList;
    }

    private void splitWorks(List<WorkbenchVo> workbenchVoList, List<UawWorkbenchMenuVo> list) {
        workbenchVoList.forEach(commentsVo -> {
            List<WorkbenchVo> replayList = splitWorks(commentsVo.getId(), list);
            commentsVo.setChildren(replayList);
        });
    }

    public List<WorkbenchVo> findMemberMenu(Long memberId, Long type) {
        List<UawWorkbenchMenu> list = this.selectJoinList(UawWorkbenchMenu.class,
                MPJWrappers.<UawWorkbenchMenu>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawWorkbenchMenu.class)
                        .leftJoin(UawWorkbenchMenu.class, UawWorkbenchMenu::getId, UawVipClassMenu::getMenuId)
                        .leftJoin(UawVipClassMenu.class, UawVipClassMenu::getClassId, UawVipClass::getId)
                        .leftJoin(UawVipClass.class, UawVipClass::getClassSerialNumber, UawVipMessage::getClassSerialNumber)
                        .eq(UawVipMessage::getMemberId, memberId)
                        .eq(UawVipMessage::getStatus, 0)
                        .eq(UawWorkbenchMenu::getWorkbenchId, type)
                        .eq(UawWorkbenchMenu::getStatus, 0)
                        .eq(UawWorkbenchMenu::getIsHidden, 0)
                        .orderByAsc(UawWorkbenchMenu::getSort)
        );
        List<UawWorkbenchMenuVo> uawWorkbenchMenuVos = new ArrayList<>();
        for (UawWorkbenchMenu uacMenuEntity : list) {
            UawWorkbenchMenuVo uawWorkbenchMenuVo = new UawWorkbenchMenuVo();
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
            BeanUtils.copyProperties(uacMenuEntity, uawWorkbenchMenuVo);
            uawWorkbenchMenuVo.setMeta(menuMetaDto);
            uawWorkbenchMenuVos.add(uawWorkbenchMenuVo);
        }
        List<WorkbenchVo> workbenchVoList = new ArrayList<>();
        if (!CommonUtil.isEmpty(list)) {
            workbenchVoList = splitWorks(0L, uawWorkbenchMenuVos);
        }
        Collections.sort(workbenchVoList);
        return workbenchVoList;
    }

    public PageList<UawWorkbenchMenu> findMenu(Long parentId, int type, Long workBenchId, Integer pageSize, Integer pageNum) {
        LambdaQueryWrapper<UawWorkbenchMenu> uawWorkbenchMenuLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uawWorkbenchMenuLambdaQueryWrapper.eq(UawWorkbenchMenu::getParentId, parentId);
        uawWorkbenchMenuLambdaQueryWrapper.eq(UawWorkbenchMenu::getWorkbenchId, workBenchId);
        uawWorkbenchMenuLambdaQueryWrapper.eq(UawWorkbenchMenu::getPlatformType, type);
        uawWorkbenchMenuLambdaQueryWrapper.eq(UawWorkbenchMenu::getStatus, 0);
        Page<UawWorkbenchMenu> page = this.page(new Page<>(pageNum, pageSize), uawWorkbenchMenuLambdaQueryWrapper);
        PageList<UawWorkbenchMenu> uawWorkbenchMenus = new PageList<>();
        uawWorkbenchMenus.setRecords(page.getRecords());
        uawWorkbenchMenus.setTotalSize(page.getTotal());
        return uawWorkbenchMenus;
    }

    public void redisMenuAll() {
        String path = "";
        Map<String, String> map = new HashMap<>();
        List<UawWorkbenchMenu> list = this.lambdaQuery().eq(UawWorkbenchMenu::getStatus, 0)
                .eq(UawWorkbenchMenu::getType, 2).list();
        for (int i = 0; i < list.size(); i++) {
            if (StringUtils.isNotEmpty(list.get(i).getButtonPath())) {
                path = path + list.get(i).getButtonPath() + ',';
                map.put(list.get(i).getButtonPath() + "||" + list.get(i).getParentId(), list.get(i).getButtonName() + "," + list.get(i).getButtonType());
            }
        }
        path = path.substring(0, path.length() - 1);
        stringRedisTemplate.opsForValue().set("AEOTRADE_All", path);
        stringRedisTemplate.delete("AEOTRADE_LOG_All");
        stringRedisTemplate.opsForHash().putAll("AEOTRADE_LOG_All", map);
    }

    public void delete(Long id) {
        this.removeById(id);
    }

    public void updateVipClassMenu(Long workbenchId, List<UawWorkbenchMenu> menus) {
        List<UawVipType> list = uawVipTypeService.lambdaQuery()
                .eq(UawVipType::getWorkbench, workbenchId)
                .eq(UawVipType::getStatus, 0).list();
        for (UawVipType vipType : list) {
            List<UawVipClass> classList = uawVipClassService.lambdaQuery()
                    .eq(UawVipClass::getTypeId, vipType.getId())
                    .eq(UawVipClass::getStatus, 0).list();
            for (UawVipClass aClass : classList) {
                for (UawWorkbenchMenu menu : menus) {
                    UawVipClassMenu uawVipClassMenu = new UawVipClassMenu();
                    uawVipClassMenu.setClassId(aClass.getId());
                    uawVipClassMenu.setMenuId(menu.getId());
                    List<UawVipClassMenu> mapperList = uawVipClassMenuMapper.lambdaQuery()
                            .eq(UawVipClassMenu::getClassId, aClass.getId())
                            .eq(UawVipClassMenu::getMenuId, menu.getId()).list();
                    if (null == mapperList || mapperList.size() == 0) {
                        uawVipClassMenuMapper.save(uawVipClassMenu);
                    }
                }
            }
        }

    }


}

package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.adminVo.UacAdminParam;
import com.aeotrade.provider.admin.adminVo.UpdateAdminPasswordParam;
import com.aeotrade.provider.admin.entiy.*;
import com.aeotrade.provider.admin.service.*;
import com.aeotrade.provider.admin.mapper.UacAdminMapper;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 后台用户表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
@Service
public class UacAdminServiceImpl extends ServiceImpl<UacAdminMapper, UacAdmin> implements UacAdminService {

    @Autowired
    private UacAdminRoleService uacAdminRoleService;
    @Autowired
    private UacRoleService uacRoleService;
    @Value("${redis.database}")
    private String REDIS_DATABASE;
    @Value("${redis.expire.common}")
    private Long REDIS_EXPIRE;
    @Value("${redis.key.admin}")
    private String REDIS_KEY_ADMIN;
    @Value("${redis.key.resourceList}")
    private String REDIS_KEY_RESOURCE_LIST;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UacDeptService uacDeptService;
    @Autowired
    private UacDeptStaffService uacDeptStaffService;
    @Autowired
    private UawWorkbenchService uawWorkbenchService;


    @Override
    public String findByUserName(String username) {
        List<UacAdmin> list = this.lambdaQuery().eq(UacAdmin::getUsername, username).eq(UacAdmin::getIsTab, 2)
                .eq(UacAdmin::getStatus, 1).list();
        UacAdmin uacAdmin = list.size() > 0 ? list.get(0) : null;
        if (uacAdmin != null) {
            List<UacRole> role = uacAdminRoleService.findRole(uacAdmin.getId());
            if (role.size() == 0) {
                return "";
            }
            uacAdmin.setLoginTime(LocalDateTime.now());
            this.updateById(uacAdmin);
            return uacAdmin.getNickName();
        }
        return "";
    }

    @Override
    public List<UacRole> findRoleByUserName(String username, String... rolename) {
        UacRole uacRole = null;
        List<UacRole> uacRoleList = new ArrayList<>();
        List<UacAdmin> list = this.lambdaQuery().eq(UacAdmin::getUsername, username).eq(UacAdmin::getIsTab, 2).list();
        UacAdmin uacAdmin = list.size() > 0 ? list.get(0) : null;
        if (uacAdmin != null) {
            List<UacRole> role = uacAdminRoleService.findRole(uacAdmin.getId());
            if (role.size() == 0) {
                return null;
            }
            for (UacRole r : role) {
                for (String rname : rolename) {
                    if (r.getName().trim().equals(rname)) {
                        uacRoleList.add(r);
                    }
                }

            }
            return uacRoleList;
        }
        return null;
    }

    @Override
    public UacAdmin findAdeminMobile(String valueOf) {
        List<UacAdmin> list = this.lambdaQuery().eq(UacAdmin::getIsTab, 2)
                .eq(UacAdmin::getStatus, 1).eq(UacAdmin::getMobile, valueOf).list();
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void updatePassword(String newpassword, Long id) {
        UacAdmin admin = this.getById(id);
        admin.setPassword(newpassword);
        this.updateById(admin);
    }

    @Override
    public UacAdmin getAdminByUsername(String username) {
//        UacAdmin example = new UacAdmin();
//        example.createCriteria().andUsernameEqualTo(username).andisTab();
//        List<UacAdmin> adminList = adminMapper.selectByExample(example);
        List<UacAdmin> adminList = this.lambdaQuery()
                .eq(UacAdmin::getUsername, username)
                .eq(UacAdmin::getIsTab, 2).list();
        if (adminList != null && adminList.size() > 0) {
            UacAdmin admin = adminList.get(0);
            String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
            stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(admin), REDIS_EXPIRE);
            return admin;
        }
        return null;
    }

    @Override
    public List<UacRole> getRoleList(Long id) {
        return uacRoleService.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRole.class)
                        .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .eq(UacAdminRole::getAdminId, id)
                        .eq(UacAdminRole::getMemberId, 0)
                        .eq(UacRole::getPlatform, 2)
                        .eq(UacRole::getStatus, 1)
        );
    }

    @Override
    public UacAdmin register(UacAdminParam uacAdminParam) {
        UacAdmin umsAdmin = new UacAdmin();
        BeanUtils.copyProperties(uacAdminParam, umsAdmin);
        umsAdmin.setCreateTime(LocalDateTime.now());
        //查询是否有相同用户名的用户
//        UacAdminExample example = new UacAdminExample();
//        example.createCriteria().andUsernameEqualTo(umsAdmin.getUsername()).andisTab();
//        List<UacAdminEntity> umsAdminList = adminMapper.selectByExample(example);
        UacAdmin findname = new UacAdmin();
        findname.setUsername(umsAdmin.getUsername());
        findname.setIsTab(2);
        List<UacAdmin> umsAdminList = this.lambdaQuery(findname).list();
        if (umsAdminList.size() > 0) {
            return null;
        }
        //将密码进行加密操作
        String encodePassword = passwordEncoder.encode(umsAdmin.getPassword());
        umsAdmin.setPassword(encodePassword);
        umsAdmin.setIsTab(2);
//        adminMapper.insert(umsAdmin.getMobile(),umsAdmin.getUsername(), umsAdmin.getPassword(), umsAdmin.getStatus(),
//                umsAdmin.getIsTab(), umsAdmin.getCreateTime(), umsAdmin.getNickName(), umsAdmin.getNote(), "cskefu");
        umsAdmin.setSecureconf("5");
        umsAdmin.setOrgi("cskefu");
        umsAdmin.setAgent((byte) 1);
        umsAdmin.setDatastatus((byte) 0);
        umsAdmin.setCallcenter((byte) 0);
        umsAdmin.setSuperadmin((byte) 0);
        umsAdmin.setAdmin((byte) 0);
        umsAdmin.setMaxuser(0);
        umsAdmin.setFans(0);
        umsAdmin.setFollows(0);
        umsAdmin.setIntegral(0);
        umsAdmin.setLogin((byte) 1);
        this.save(umsAdmin);
        return umsAdmin;

    }

    @Override
    public Page<UacAdmin> findAdminlist(String keyword, Integer pageSize, Integer pageNum) {
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryChainWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            uacAdminLambdaQueryChainWrapper.like(UacAdmin::getUsername, keyword)
                    .or().like(UacAdmin::getNickName, keyword);
        }
        uacAdminLambdaQueryChainWrapper.eq(UacAdmin::getIsTab, 2);
        uacAdminLambdaQueryChainWrapper.orderByDesc(UacAdmin::getCreateTime);
        Page<UacAdmin> list = this.page(new Page<>(pageNum, pageSize), uacAdminLambdaQueryChainWrapper);
        return list;
    }

    @Override
    public List<String> findRoleName(Long id) {
        return uacRoleService.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRole.class)
                        .innerJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .eq(UacAdminRole::getAdminId, id)
                        .eq(UacAdminRole::getMemberId, 0)
                        .eq(UacRole::getPlatform, 2)
                        .eq(UacRole::getStatus, 1)
        ).stream().map(UacRole::getName).collect(Collectors.toList());
    }

    @Override
    public UacAdmin getItem(Long id) {
        return this.getById(id);
    }

    @Override
    public int updateAdmin(Long id, UacAdmin admin) {
        admin.setId(id);
        UacAdmin rawAdmin = this.getById(id);
        if (!rawAdmin.getUsername().equals(admin.getUsername())) {
            //查询是否有相同用户名的用户
//            UacAdminExample example = new UacAdminExample();
//            example.createCriteria().andUsernameEqualTo(admin.getUsername());
//            List<UacAdminEntity> umsAdminList = adminMapper.selectByExample(example);
            UacAdmin findname = new UacAdmin();
            findname.setUsername(admin.getUsername());
            findname.setIsTab(2);
            List<UacAdmin> umsAdminList = this.lambdaQuery(findname).list();
            if (umsAdminList.size() > 0) {
                return 3;
            }
        }
        if (rawAdmin.getPassword().equals(admin.getPassword())) {
            //与原加密密码相同的不需要修改
            admin.setPassword(null);
        } else {
            //与原加密密码不同的需要加密修改
            if (StringUtils.isEmpty(admin.getPassword())) {
                admin.setPassword(null);
            } else {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            }
        }
        admin.setMobile(admin.getMobile());
        Boolean count = this.updateById(admin);
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + rawAdmin.getUsername();
        stringRedisTemplate.delete(key);
        return 1;
    }

    @Override
    public int updatePasswords(UpdateAdminPasswordParam param) {
        if (StringUtils.isEmpty(param.getUsername()) || StringUtils.isEmpty(param.getOldPassword()) || StringUtils.isEmpty(param.getNewPassword())) {
            return -1;
        }
        UacAdmin findname = new UacAdmin();
        findname.setUsername(param.getUsername());
        findname.setIsTab(2);
        List<UacAdmin> adminList = this.lambdaQuery(findname).list();
        if (adminList.size() == 0) {
            return -2;
        }
        UacAdmin umsAdmin = adminList.get(0);
        if (!passwordEncoder.matches(param.getOldPassword(), umsAdmin.getPassword())) {
            return -3;
        }
        umsAdmin.setPassword(passwordEncoder.encode(param.getNewPassword()));
        this.updateById(umsAdmin);
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + umsAdmin.getUsername();
        stringRedisTemplate.delete(key);
        return 1;
    }

    @Override
    public int delete(Long id) {
        UacAdmin admin = this.getById(id);
        String key = REDIS_DATABASE + ":" + REDIS_KEY_ADMIN + ":" + admin.getUsername();
        stringRedisTemplate.delete(key);
        Boolean count = this.removeById(id);
//        adminMapper.deleteByAdminId(id);
        UacAdminRole uacAdminRole = new UacAdminRole();
        uacAdminRole.setAdminId(id);
        uacAdminRole.setMemberId(0L);
        uacAdminRoleService.removeById(uacAdminRole);
        String delkey = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + id;
        stringRedisTemplate.delete(delkey);
        return 1;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds, Long workBnechId) {
        int count = roleIds == null ? 0 : roleIds.size();
        //先删除原来的关系
        LambdaQueryWrapper<UacAdminRole> uacAdminLambdaQueryChainWrapper = new LambdaQueryWrapper<>();
        uacAdminLambdaQueryChainWrapper.eq(UacAdminRole::getAdminId, adminId);
        uacAdminLambdaQueryChainWrapper.eq(UacAdminRole::getOrgi, workBnechId);
        uacAdminLambdaQueryChainWrapper.eq(UacAdminRole::getMemberId, 0);
        uacAdminRoleService.remove(uacAdminLambdaQueryChainWrapper);
        //建立新关系
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UacAdminRole> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                UacAdminRole roleRelation = new UacAdminRole();
                roleRelation.setAdminId(adminId);
                roleRelation.setRoleId(roleId);
                roleRelation.setMemberId(0L);
                roleRelation.setOrgi(String.valueOf(workBnechId));
//                list.add(roleRelation);
                uacAdminRoleService.save(roleRelation);
            }
//            System.out.println("list" + list);
//            adminRoleRelationDao.insertList(list);
        }
        String key = REDIS_DATABASE + ":" + REDIS_KEY_RESOURCE_LIST + ":" + adminId;
        stringRedisTemplate.delete(key);
        return count;
    }

    @Override
    public List<UacRole> getHmmRoleList(Long adminId, Long organ, Long workBnechId, Long memberId) {
        if (null != organ) {
            return uacRoleService.selectJoinList(UacRole.class,
                    MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UacRole.class)
                            .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                            .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                            .eq(UacAdmin::getStaffId, adminId)
                            .eq(UacAdmin::getIsTab, 1)
                            .eq(UacRole::getOrgan, organ)
                            .eq(UacAdminRole::getOrgi, workBnechId)
                            .eq(UacAdminRole::getMemberId, memberId));
        }
        return uacRoleService.selectJoinList(UacRole.class,
                MPJWrappers.<UacRole>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UacRole.class)
                        .leftJoin(UacAdminRole.class, UacAdminRole::getRoleId, UacRole::getId)
                        .leftJoin(UacAdmin.class, UacAdmin::getId, UacAdminRole::getAdminId)
                        .eq(UacAdmin::getStaffId, adminId)
                        .eq(UacAdmin::getIsTab, 1)
                        .eq(UacAdminRole::getOrgi, workBnechId)
                        .eq(UacAdminRole::getMemberId, memberId));
    }

    @Override
    public UacAdmin findByStaffId(Long staffId) {
        List<UacAdmin> list = this.lambdaQuery().eq(UacAdmin::getStaffId, staffId).eq(UacAdmin::getIsTab, 1).list();
        return list.size() > 0 ? list.get(0) : null;
    }

    @Override
    public void delectAdminRole(Long id, Long memberId, Long workBnechId) {
//        List<Long> longs = uacRoleService.lambdaQuery()
//                .in(UacRole::getOrgid, memberId).list()
//                .stream().map(UacRole::getId).collect(Collectors.toList());
        List<UacAdminRole> list = uacAdminRoleService.lambdaQuery()
                .eq(UacAdminRole::getAdminId, id)
                .eq(UacAdminRole::getOrgi, workBnechId)
                .eq(UacAdminRole::getMemberId, memberId)
                .list();
        uacAdminRoleService.removeBatchByIds(list);
        UacAdmin uacAdmin = this.getById(id);
        UawWorkbench uawWorkbench = uawWorkbenchService.getById(workBnechId);
        if(null!=uawWorkbench && uawWorkbench.getPlatformType()==1){
            List<UacDept> deptIds = uacDeptService.selectJoinList(UacDept.class,
                    MPJWrappers.<UacDept>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UacDept.class)
                            .leftJoin(UacDeptStaff.class, UacDeptStaff::getDeptId, UacDept::getId)
                            .eq(UacDeptStaff::getStaffId, uacAdmin.getStaffId())
                            .eq(UacDept::getMemberId, memberId));
            if(deptIds.size()!=0){
                for (UacDept uacDept : deptIds) {
                    LambdaQueryWrapper<UacDeptStaff> uacDeptStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    uacDeptStaffLambdaQueryWrapper.eq(UacDeptStaff::getDeptId, uacDept.getId());
                    uacDeptStaffLambdaQueryWrapper.eq(UacDeptStaff::getStaffId, uacAdmin.getStaffId());
                    uacDeptStaffService.remove(uacDeptStaffLambdaQueryWrapper);
                }
            }
        }

    }

    @Override
    public void staffUpdate(Long id, String roleId, Long workBnechId, Long memberId, String deptId) {

        if (StringUtils.isNotEmpty(roleId)) {
            String[] split = roleId.split(",");
            for (String s : split) {
                UacAdminRole uacAdminRole = new UacAdminRole();
                uacAdminRole.setAdminId(id);
                uacAdminRole.setMemberId(memberId);
                uacAdminRole.setRoleId(Long.valueOf(s));
                uacAdminRole.setOrgi(String.valueOf(workBnechId));
                uacAdminRoleService.save(uacAdminRole);
            }
            long byId = this.getById(id).getStaffId();
            List<UacAdminRole> list = uacAdminRoleService.lambdaQuery()
                    .eq(UacAdminRole::getAdminId, id)
                    .eq(UacAdminRole::getMemberId, memberId).list();
            if(list.size()!=0){
                List<Long> longSet = list.stream().map(UacAdminRole::getRoleId).collect(Collectors.toList());
                stringRedisTemplate.opsForValue().set("AEOTRADE_ROLE:" + byId + memberId, StringUtils.join(longSet.toArray(), ","));
            }
        }
        UawWorkbench uawWorkbench = uawWorkbenchService.getById(workBnechId);
        if(null!=uawWorkbench && uawWorkbench.getPlatformType()==1) {
            if (StringUtils.isNotEmpty(deptId)) {
                String[] deptIds = deptId.split(",");
                UacAdmin uacAdmin = this.getById(id);
                for (String s : deptIds) {
                    UacDeptStaff uacDeptStaff = new UacDeptStaff();
                    uacDeptStaff.setDeptId(Long.valueOf(s));
                    uacDeptStaff.setStaffId(uacAdmin.getStaffId());
                    uacDeptStaffService.save(uacDeptStaff);
                }
            }
        }
    }
}

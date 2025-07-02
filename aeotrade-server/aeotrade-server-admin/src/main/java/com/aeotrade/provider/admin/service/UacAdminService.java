package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.adminVo.UacAdminParam;
import com.aeotrade.provider.admin.adminVo.UpdateAdminPasswordParam;
import com.aeotrade.provider.admin.entiy.UacAdmin;
import com.aeotrade.provider.admin.entiy.UacRole;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * <p>
 * 后台用户表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-10
 */
public interface UacAdminService extends MPJBaseService<UacAdmin> {

    String findByUserName(String username);

    List<UacRole> findRoleByUserName(String username, String... rolename);

    UacAdmin findAdeminMobile(String valueOf);

    void updatePassword(String newpassword, Long id);

    UacAdmin getAdminByUsername(String username);

    List<UacRole> getRoleList(Long id);

    UacAdmin register(UacAdminParam uacAdminParam);

    Page<UacAdmin> findAdminlist(String keyword, Integer pageSize, Integer pageNum);

    List<String> findRoleName(Long id);

    UacAdmin getItem(Long id);

    int updateAdmin(Long id, UacAdmin admin);

    int updatePasswords(UpdateAdminPasswordParam updatePasswordParam);

    int delete(Long id);

    int updateRole(Long adminId, List<Long> roleIds, Long workBnechId);

    List<UacRole> getHmmRoleList(Long adminId, Long organ, Long workBnechId, Long memberId);

    UacAdmin findByStaffId(Long staffId);

    void delectAdminRole(Long id, Long memberId, Long workBnechId);

    void staffUpdate(Long id, String roleId, Long workBnechId,Long memberId,String deptId);
}

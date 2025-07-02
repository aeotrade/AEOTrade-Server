package com.aeotrade.provider.mamber.service;


import com.aeotrade.provider.mamber.entity.AppCategory;
import com.aeotrade.provider.mamber.entity.AppRole;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2025/3/19 11:23
 */
public interface AppRoleService extends MPJBaseService<AppRole> {
    List<AppRole> appManage(String memberId, String vipTypeId, String categroyId, String appName);

    List<AppCategory> findvipAll(String vipTypeId, String memberId, String roles);

    List<AppRole> appManageZiJian(String memberId, String vipTypeId, String appName);
}

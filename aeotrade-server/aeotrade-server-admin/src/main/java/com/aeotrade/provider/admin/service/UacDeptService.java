package com.aeotrade.provider.admin.service;


import com.aeotrade.provider.admin.entiy.UacDept;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;

/**
 * <p>
 * 部门表 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
public interface UacDeptService extends MPJBaseService<UacDept> {
    List<Long> findListById(Long deptId);

}

package com.aeotrade.provider.mamber.service;


import com.aeotrade.provider.mamber.entity.UacStaff;
import com.github.yulichang.base.MPJBaseService;

/**
 * <p>
 * 企业员工 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
public interface UacStaffService extends MPJBaseService<UacStaff> {

    UacStaff findByid(Long staffId);

    void updateStaff(UacStaff staff);
}

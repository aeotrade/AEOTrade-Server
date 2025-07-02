package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UacStaff;
import com.aeotrade.provider.mamber.mapper.UacStaffMapper;
import com.aeotrade.provider.mamber.service.UacStaffService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.base.MPJBaseService;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 企业员工 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
@DS("aeotrade")
public class UacStaffServiceImpl extends MPJBaseServiceImpl<UacStaffMapper, UacStaff> implements UacStaffService {


    @Override
    public UacStaff findByid(Long staffId) {
        return this.getById(staffId);
    }

    @Override
    public void updateStaff(UacStaff staff) {
        this.updateById(staff);
    }
}

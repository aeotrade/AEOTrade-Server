package com.aeotrade.server.message.service.impl;


import com.aeotrade.server.message.mapper.UacStaffMapper;
import com.aeotrade.server.message.model.UacStaff;
import com.aeotrade.server.message.service.UacStaffService;
import com.baomidou.dynamic.datasource.annotation.DS;
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


}

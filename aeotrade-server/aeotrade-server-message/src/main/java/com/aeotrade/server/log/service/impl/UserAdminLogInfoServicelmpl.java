package com.aeotrade.server.log.service.impl;

import com.aeotrade.server.log.model.UserAdminLogInfo;
import com.aeotrade.server.log.mapper.UserAdminLogInfoMapper;
import com.aeotrade.server.log.service.UserAdminLogInfoService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 17:02
 */
@Service
@DS("log")
public class UserAdminLogInfoServicelmpl  extends ServiceImpl<UserAdminLogInfoMapper, UserAdminLogInfo> implements UserAdminLogInfoService {
}

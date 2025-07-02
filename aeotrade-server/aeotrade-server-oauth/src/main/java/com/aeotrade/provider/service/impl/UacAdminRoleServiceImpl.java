package com.aeotrade.provider.service.impl;

import com.aeotrade.provider.mapper.UacAdminRoleMapper;
import com.aeotrade.provider.model.UacAdminRole;
import com.aeotrade.provider.service.UacAdminRoleService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 后台用户和角色关系表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@DS("aeotrade")
public class UacAdminRoleServiceImpl extends ServiceImpl<UacAdminRoleMapper, UacAdminRole> implements UacAdminRoleService {

}

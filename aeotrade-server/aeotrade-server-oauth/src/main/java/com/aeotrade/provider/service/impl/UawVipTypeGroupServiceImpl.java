package com.aeotrade.provider.service.impl;

import com.aeotrade.provider.mapper.UawVipTypeGroupMapper;
import com.aeotrade.provider.model.UawVipTypeGroup;
import com.aeotrade.provider.service.UawVipTypeGroupService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员类型分组表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@DS("mall")
public class UawVipTypeGroupServiceImpl extends ServiceImpl<UawVipTypeGroupMapper, UawVipTypeGroup> implements UawVipTypeGroupService {

}

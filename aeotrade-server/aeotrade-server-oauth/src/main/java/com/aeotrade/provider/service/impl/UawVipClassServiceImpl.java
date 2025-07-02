package com.aeotrade.provider.service.impl;

import com.aeotrade.provider.mapper.UawVipClassMapper;
import com.aeotrade.provider.model.UawVipClass;
import com.aeotrade.provider.service.UawVipClassService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会员等级表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@DS("mall")
public class UawVipClassServiceImpl extends MPJBaseServiceImpl<UawVipClassMapper, UawVipClass> implements UawVipClassService {

}

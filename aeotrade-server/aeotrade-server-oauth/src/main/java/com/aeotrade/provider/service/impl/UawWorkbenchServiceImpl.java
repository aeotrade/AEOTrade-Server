package com.aeotrade.provider.service.impl;

import com.aeotrade.provider.mapper.UawWorkbenchMapper;
import com.aeotrade.provider.model.UawWorkbench;
import com.aeotrade.provider.service.UawWorkbenchService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
@DS("mall")
public class UawWorkbenchServiceImpl extends MPJBaseServiceImpl<UawWorkbenchMapper, UawWorkbench> implements UawWorkbenchService {

}

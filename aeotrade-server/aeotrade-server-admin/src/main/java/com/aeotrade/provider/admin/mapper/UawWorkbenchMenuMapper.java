package com.aeotrade.provider.admin.mapper;

import com.aeotrade.provider.admin.entiy.UawWorkbenchMenu;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-10 14:46
 */
@Mapper
@DS("mall")
public interface UawWorkbenchMenuMapper extends MPJBaseMapper<UawWorkbenchMenu> {
}

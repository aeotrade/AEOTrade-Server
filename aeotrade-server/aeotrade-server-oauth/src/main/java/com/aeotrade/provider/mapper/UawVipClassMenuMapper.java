package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.UawVipClassMenu;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-08 10:22
 */
@Mapper
@DS("mall")
public interface UawVipClassMenuMapper extends MPJBaseMapper<UawVipClassMenu> {
}

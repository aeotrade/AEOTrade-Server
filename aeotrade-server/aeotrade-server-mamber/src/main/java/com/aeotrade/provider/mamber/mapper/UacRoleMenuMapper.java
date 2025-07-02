package com.aeotrade.provider.mamber.mapper;

import com.aeotrade.provider.mamber.entity.UacRoleMenu;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-09 9:34
 */
@Mapper
@DS("aeotrade")
public interface UacRoleMenuMapper extends MPJBaseMapper<UacRoleMenu> {
}

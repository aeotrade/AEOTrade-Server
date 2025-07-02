package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.UacAdminRole;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-06 14:07
 */
@Mapper
@DS("aeotrade")
public interface UacAdminRoleMapper extends MPJBaseMapper<UacAdminRole> {
}

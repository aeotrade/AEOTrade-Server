package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.UacAdminRole;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 后台用户和角色关系表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Mapper
@DS("aeotrade")
public interface UacAdminRoleMapper extends BaseMapper<UacAdminRole> {

}

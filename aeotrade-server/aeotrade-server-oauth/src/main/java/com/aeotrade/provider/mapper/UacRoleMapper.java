package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.UacRole;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Mapper
@DS("aeotrade")
public interface UacRoleMapper extends BaseMapper<UacRole> {

}

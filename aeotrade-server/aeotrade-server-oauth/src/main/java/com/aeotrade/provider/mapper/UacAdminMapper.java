package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.UacAdmin;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 后台用户表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Mapper
@DS("aeotrade")
public interface UacAdminMapper extends BaseMapper<UacAdmin> {

}

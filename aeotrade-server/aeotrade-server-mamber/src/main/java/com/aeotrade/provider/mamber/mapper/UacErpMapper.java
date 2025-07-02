package com.aeotrade.provider.mamber.mapper;

import com.aeotrade.provider.mamber.entity.UacErp;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-08 10:20
 */
@Mapper
@DS("aeotrade")
public interface UacErpMapper extends BaseMapper<UacErp> {
}

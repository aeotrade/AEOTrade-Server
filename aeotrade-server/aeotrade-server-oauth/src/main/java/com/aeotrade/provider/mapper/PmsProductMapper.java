package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.PmsProduct;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 商品信息 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Mapper
@DS("mall")
public interface PmsProductMapper extends MPJBaseMapper<PmsProduct> {

}

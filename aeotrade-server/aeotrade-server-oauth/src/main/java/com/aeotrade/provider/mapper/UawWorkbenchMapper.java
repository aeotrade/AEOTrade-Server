package com.aeotrade.provider.mapper;

import com.aeotrade.provider.model.UawWorkbench;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
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
@DS("mall")
public interface UawWorkbenchMapper extends MPJBaseMapper<UawWorkbench> {

}

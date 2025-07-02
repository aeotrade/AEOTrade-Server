package com.aeotrade.provider.admin.mapper;


import com.aeotrade.provider.admin.entiy.UawWorkbench;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Mapper
@DS("mall")
public interface UawWorkbenchMapper extends BaseMapper<UawWorkbench> {

}

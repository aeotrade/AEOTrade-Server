package com.aeotrade.provider.admin.mapper;



import com.aeotrade.provider.admin.entiy.UawAptitudes;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-06 10:11
 */
@Mapper
@DS("mall")
public interface UawAptitudesMapper extends BaseMapper<UawAptitudes> {
}

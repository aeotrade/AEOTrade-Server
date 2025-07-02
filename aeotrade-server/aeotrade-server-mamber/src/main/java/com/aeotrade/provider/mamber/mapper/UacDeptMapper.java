package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.UacDept;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-30 10:44
 */
@Mapper
@DS("aeotrade")
public interface UacDeptMapper extends MPJBaseMapper<UacDept> {
}

package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.UacStaff;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 企业员工 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@DS("aeotrade")
@Mapper
public interface UacStaffMapper extends MPJBaseMapper<UacStaff> {

}

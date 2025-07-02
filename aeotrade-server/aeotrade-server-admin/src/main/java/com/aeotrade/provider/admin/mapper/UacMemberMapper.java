package com.aeotrade.provider.admin.mapper;


import com.aeotrade.provider.admin.entiy.UacMember;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 企业表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Mapper
@DS("aeotrade")
public interface UacMemberMapper extends MPJBaseMapper<UacMember> {

}

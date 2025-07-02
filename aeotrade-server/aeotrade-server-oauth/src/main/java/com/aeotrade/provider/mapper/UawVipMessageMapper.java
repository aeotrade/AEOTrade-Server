package com.aeotrade.provider.mapper;


import com.aeotrade.provider.model.UawVipMessage;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 会员信息表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Mapper
@DS("mall")
public interface UawVipMessageMapper extends MPJBaseMapper<UawVipMessage> {

}

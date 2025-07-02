package com.aeotrade.provider.admin.mapper;


import com.aeotrade.provider.admin.entiy.UawVipMessage;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 会员信息表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Mapper
@DS("mall")
public interface UawVipMessageMapper extends BaseMapper<UawVipMessage> {

}

package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.UacOauthClientDetails;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-10-26
 */
@Mapper
@DS("aeotrade")
public interface UacOauthClientDetailsMapper extends BaseMapper<UacOauthClientDetails> {

}

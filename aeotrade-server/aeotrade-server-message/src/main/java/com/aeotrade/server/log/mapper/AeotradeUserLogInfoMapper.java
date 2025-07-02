package com.aeotrade.server.log.mapper;

import com.aeotrade.server.log.model.AeotradeUserLogInfo;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 16:59
 */
@Mapper
@DS("log")
public interface AeotradeUserLogInfoMapper extends BaseMapper<AeotradeUserLogInfo> {

}

package com.aeotrade.server.log.mapper;

import com.aeotrade.server.log.model.UserDocumentLog;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@Mapper
@DS("log")
public interface UserDocumentLogMapper extends BaseMapper<UserDocumentLog> {

}

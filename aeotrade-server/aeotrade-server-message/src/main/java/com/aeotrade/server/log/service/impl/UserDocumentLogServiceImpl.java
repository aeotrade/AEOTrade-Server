package com.aeotrade.server.log.service.impl;

import com.aeotrade.server.log.model.UserDocumentLog;
import com.aeotrade.server.log.mapper.UserDocumentLogMapper;
import com.aeotrade.server.log.service.UserDocumentLogService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yewei
 * @since 2022-10-27
 */
@Service
@DS("log")
public class UserDocumentLogServiceImpl extends ServiceImpl<UserDocumentLogMapper, UserDocumentLog> implements UserDocumentLogService {

}

package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UacOauthClientDetails;
import com.aeotrade.provider.mamber.mapper.UacOauthClientDetailsMapper;
import com.aeotrade.provider.mamber.service.UacOauthClientDetailsService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-10-26
 */
@Service
@DS("aeotrade")
public class UacOauthClientDetailsServiceImpl extends ServiceImpl<UacOauthClientDetailsMapper, UacOauthClientDetails> implements UacOauthClientDetailsService {

}

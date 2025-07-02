package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.WxCatCud;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: yewei
 * @Date: 2020/4/1 11:15
 */
@Mapper
@DS("weixin")
public interface WxCatCudMapper extends BaseMapper<WxCatCud> {
}

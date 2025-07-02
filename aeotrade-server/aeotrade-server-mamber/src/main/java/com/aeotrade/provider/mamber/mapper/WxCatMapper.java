package com.aeotrade.provider.mamber.mapper;


import com.aeotrade.provider.mamber.entity.WxCat;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: yewei
 * @Date: 2020/3/30 19:45
 */
@Mapper
@DS("weixin")
public interface WxCatMapper extends BaseMapper<WxCat> {

}

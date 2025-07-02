package com.aeotrade.server.log.service.impl;

import com.aeotrade.server.log.config.AddressUtil;
import com.aeotrade.server.log.mapper.AeotradeUserLogInfoMapper;
import com.aeotrade.server.log.model.AeotradeUserLogInfo;
import com.aeotrade.server.log.service.AeotradeUserLogInfoService;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @Auther: 吴浩
 * @Date: 2023-03-17 17:02
 */
@Service
@DS("log")
public class AeotradeUserLogInfoServicelmpl extends ServiceImpl<AeotradeUserLogInfoMapper, AeotradeUserLogInfo> implements AeotradeUserLogInfoService {

    public void insertAeotradeUserLog(String payload) {
        AeotradeUserLogInfo aeotradeUserLogInfo = JSONObject.parseObject(payload, AeotradeUserLogInfo.class);
        //根据ip地址获取用户所在城市信息
        String cityInfo = AddressUtil.getCityInfo(aeotradeUserLogInfo.getIp());
        aeotradeUserLogInfo.setIp_address(cityInfo);
        if(StringUtils.isNotEmpty(cityInfo)){
            String[] strings = cityInfo.split("\\|");
            if(!strings[0].equals("中国")){
                aeotradeUserLogInfo.setIp_province(strings[0]);
                aeotradeUserLogInfo.setIp_city(strings[0]);
            }else{
                aeotradeUserLogInfo.setIp_province(strings[2]);
                aeotradeUserLogInfo.setIp_city(strings[3]);
            }
        }
        this.baseMapper.insert(aeotradeUserLogInfo);
    }
}
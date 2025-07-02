package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.SmsHomeAdvertise;
import com.aeotrade.provider.mamber.vo.SmsHomeAdvertiseResut;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 17:34
 */
public interface SmsHomeAdvertiseService extends IService<SmsHomeAdvertise> {
    SmsHomeAdvertiseResut pageList(String name, Integer type, String endTime, Integer pageSize, Integer pageNum);
}

package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.SmsHomeAdvertise;
import com.aeotrade.provider.mamber.mapper.SmsHomeAdvertiseMapper;
import com.aeotrade.provider.mamber.service.SmsHomeAdvertiseService;
import com.aeotrade.provider.mamber.vo.SmsHomeAdvertiseResut;
import com.aeotrade.provider.mamber.vo.SmsHomeAdvertiseVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 17:34
 */
@Service
public class SmsHomeAdvertiseServiceImpl  extends ServiceImpl<SmsHomeAdvertiseMapper, SmsHomeAdvertise> implements SmsHomeAdvertiseService {
    @Override
    public SmsHomeAdvertiseResut pageList(String name, Integer type, String endTime, Integer pageSize, Integer pageNum) {
        LambdaQueryWrapper<SmsHomeAdvertise> smsHomeAdvertiseLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            smsHomeAdvertiseLambdaQueryWrapper.like(SmsHomeAdvertise::getName,name);
        }
        if (type != null) {
            smsHomeAdvertiseLambdaQueryWrapper.eq(SmsHomeAdvertise::getType,type);
        }
        if (!StringUtils.isEmpty(endTime)) {
            String startStr = endTime + " 00:00:00";
            String endStr = endTime + " 23:59:59";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = null;
            try {
                start = sdf.parse(startStr);
            } catch (ParseException e) {
                log.warn(e.getMessage());
            }
            Date end = null;
            try {
                end = sdf.parse(endStr);
            } catch (ParseException e) {
                log.warn(e.getMessage());
            }
            if (start != null && end != null) {
                smsHomeAdvertiseLambdaQueryWrapper.between(SmsHomeAdvertise::getEndTime,start,end);
            }
        }
        smsHomeAdvertiseLambdaQueryWrapper.orderByAsc(SmsHomeAdvertise::getSort);
        List<SmsHomeAdvertiseVO > voList = new ArrayList<>();
        Page<SmsHomeAdvertise> page = this.page(new Page<>(pageNum, pageSize), smsHomeAdvertiseLambdaQueryWrapper);
        page.getRecords().forEach(i->{
            SmsHomeAdvertiseVO vo = new SmsHomeAdvertiseVO();
            BeanUtils.copyProperties( i,vo);
            vo.setStartTime(i.getStartTime().getTime());
            vo.setEndTime(i.getEndTime().getTime());
            voList.add(vo);
        });
        SmsHomeAdvertiseResut sms = new SmsHomeAdvertiseResut();
        sms.setList(voList);
        sms.setTotal(Math.toIntExact(page.getTotal()));
        return sms;
    }
}

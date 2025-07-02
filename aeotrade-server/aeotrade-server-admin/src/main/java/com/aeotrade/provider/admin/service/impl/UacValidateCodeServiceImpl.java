package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.provider.admin.entiy.UacValidateCode;
import com.aeotrade.provider.admin.mapper.UacValidateCodeMapper;
import com.aeotrade.provider.admin.service.UacValidateCodeService;
import com.aeotrade.validator.SmsService;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class UacValidateCodeServiceImpl extends ServiceImpl<UacValidateCodeMapper, UacValidateCode> implements UacValidateCodeService {
    @Autowired
    private SmsService smsService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    /**
     * 发送验证码
     * @param phone
     * @param code
     * @throws Exception
     */
    @Transactional(rollbackFor = Exception.class)
    public int sendSmsValidateCode(String phone,String code) throws Exception{
        SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd");
        List<UacValidateCode> uacValidateCodes =this.lambdaQuery().eq(UacValidateCode::getPhone,phone)
                .like(UacValidateCode::getCreateTime,sf.format(new Date(System.currentTimeMillis())))
                .orderByDesc(UacValidateCode::getCreateTime).list();
        if(uacValidateCodes.size()==20){
            return 20;
        }
        if(uacValidateCodes.size()>=5 && System.currentTimeMillis()-uacValidateCodes.get(4).getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()<3600000L){
            return 5;
        }
        if(uacValidateCodes.size()>=1 && System.currentTimeMillis()-uacValidateCodes.get(0).getCreateTime().toInstant(ZoneOffset.of("+8")).toEpochMilli()<60000L){
            return 2;
        }
        //发送短信验证码
//        System.out.println("要发送短信了");
        SendSmsResponse sendSmsResponse = smsService.sendBindPhoneCode(phone,code);
        redisTemplate.opsForValue().set(AeoConstant.SMSREDIS_KEY+phone,code,600, TimeUnit.SECONDS);
        //将短信结果更新到对应的验证码结果上
        save(phone,code,sendSmsResponse.getBizId());
        return 1;
    }


    public Long save(String phone,String code,String bizId) throws Exception{
        UacValidateCode uacValidateCode=new UacValidateCode();
        uacValidateCode.setId(null);
        uacValidateCode.setPhone(phone);
        uacValidateCode.setCode(code);
        uacValidateCode.setExpireTime(LocalDateTime.from(LocalDateTime.now().atOffset(ZoneOffset.of("Z")).plusSeconds(300L)));
        uacValidateCode.setCreateTime(LocalDateTime.now());
        uacValidateCode.setCodeType("staff_bind");
        uacValidateCode.setStatus(0);
        uacValidateCode.setSmsBizId(bizId);
        this.save(uacValidateCode);
        return uacValidateCode.getId();
    }
}

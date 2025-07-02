package com.aeotrade.provider.mamber.utils;


import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

@Slf4j
public class TimestampUlit {

    public static Timestamp timestamp(String tsStr) {
        //Date——>Timestamp类型转换
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        try {
            ts = Timestamp.valueOf(tsStr);
            return ts;
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        return null;
    }

    public static long DeclareTimer(LocalDateTime timestamp) throws ParseException {
        //获取当前时间
        System.out.println("当前时间：" + timestamp);
        //定义时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = dateFormat.format(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //此处转换为毫秒数
        long millionSeconds = sdf.parse(str).getTime();// 毫秒
        System.out.println("毫秒数：" + millionSeconds);
        return millionSeconds;
    }


}

package com.aeotrade.provider.mamber.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
/**
 * 生成订单唯一主键，纯数字
 */
public class KeyUilt {
    /**
     * 生成主键id
     * 时间+随机数
     *
     * @return
     */
    public  static synchronized String generateUniqueKey() {
        Random random = new Random();
        // 随机数的量 自由定制，这是9位随机数
        Integer r = random.nextInt(987654321) + 987654321;

        // 返回  13位时间
        Long timeMillis = System.currentTimeMillis();

        // 返回  17位时间
        DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeStr = sdf.format(new Date());

        // 13位毫秒+9位随机数
        ///return  timeMillis + String.valueOf(r);
        // 17位时间+9位随机数
        return timeStr + r;
    }
}

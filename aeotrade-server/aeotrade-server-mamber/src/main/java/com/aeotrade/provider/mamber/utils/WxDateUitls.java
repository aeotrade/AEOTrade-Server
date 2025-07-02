package com.aeotrade.provider.mamber.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: yewei
 * @Date: 2020/6/15 14:09
 */
public class WxDateUitls {

    public static long   getDateTimeStamp(Date date){

        return   date.getTime()/1000;
    }

}

package com.nyx.bot.utils;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    /**
     * 格式化日期 以 yyyy-MM-dd HH:mm:ss 格式
     * @param date 日期
     * @return 格式化之后的字符串日期
     */
    public static String format(Date date){
        String formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").toString();
        return new SimpleDateFormat(formatter).format(date);
    }

}

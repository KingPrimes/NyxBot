package com.nyx.bot.utils;

import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public static String yyyy = "yyyy-MM-dd HH:mm:ss";
    public static String MM = "MM-dd HH:mm:ss";
    public static String dd = "dd HH:mm:ss";
    public static String hh = "HH:mm:ss";
    public static String mm = "mm:ss";


    /**
     * 格式化日期 以 yyyy-MM-dd HH:mm:ss 格式
     *
     * @param date 日期
     * @return 格式化之后的字符串日期
     */
    public static String format(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 取两个日期的时间差
     *
     * @param endDate   结束日期
     * @param startDate 开始日期
     * @return 动态返回计算结果
     */
    public static String getDiff(Date endDate, Date startDate){
        return getDiff(endDate,startDate,true);
    }


    /**
     * 取两个日期的时间差
     *
     * @param endDate   结束日期
     * @param startDate 开始日期
     * @param falg      false 返回 yyyy MM dd HH:mm:ss
     *                  true 返回 yyyy年MM月dd日HH时mm分ss秒
     * @return 动态返回计算结果
     */
    public static String getDiff(Date endDate, Date startDate, @Nullable Boolean falg) {
        if (falg == null) falg = true;
        long yy = 1000L * 60 * 60 * 24 * 365;
        long mm = 1000L * 60 * 60 * 24 * 30;
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startDate.getTime();
        //计算差多少年
        long years = diff / yy;
        //计算差多少月
        long diffSeconds = diff % yy / mm;
        // 计算差多少天
        long days = diff % yy % mm / nd;
        // 计算差多少小时
        long hours = diff % nd / nh;
        // 计算差多少分钟
        long minutes = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        long sec = diff % nd % nh % nm / ns;

        //根据相差的时间返回不同的格式
        if (years != 0) {
            if (falg) {
                return years + "年" + diffSeconds + "月" + days + "日" + hours + "时" + minutes + "分" + sec + "秒";
            }
            return years + " " + diffSeconds + " " + days + " " + hours + ":" + minutes + ":" + sec;
        }
        if (diffSeconds != 0) {
            if (falg) {
                return diffSeconds + "月" + days + "日" + hours + "时" + minutes + "分" + sec + "秒";
            }
            return diffSeconds + " " + days + " " + hours + ":" + minutes + ":" + sec;
        }
        if (days != 0) {
            if (falg) {
                return days + "日" + hours + "时" + minutes + "分" + sec + "秒";
            }
            return days + " " + hours + ":" + minutes + ":" + sec;
        }
        if (hours != 0) {
            if (falg) {
                return hours + "时" + minutes + "分" + sec + "秒";
            }
            return hours + ":" + minutes + ":" + sec;

        }
        if (minutes != 0) {
            if (falg) {
                return minutes + "分" + sec + "秒";
            }
            return minutes + ":" + sec;
        } else {
            if (falg) {
                return sec + "秒";
            }
            return String.valueOf(sec);
        }
    }

    /**
     * 计算两个时间相差的小时
     *
     * @param endDate 结束时间
     * @param nowDate 现在的时间
     * @return 相差的小时
     */
    public static long getDateHour(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        return diff % nd / nh;
    }

    /**
     * 取两个时间相差的分钟
     *
     * @param endDate 结束时间
     * @param nowDate 现在的时间
     * @return 相差的分钟
     */
    public static long getDateMin(Date endDate, Date nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        return diff % nd % nh / nm;
    }

    /**
     * 日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }


}

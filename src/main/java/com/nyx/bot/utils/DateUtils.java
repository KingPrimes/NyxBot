package com.nyx.bot.utils;

import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static final Long YEARS = 31536000000L;
    public static final Long MONTHS = 2592000000L;
    public static final Long DAYS = 86400000L;
    public static final Long HOURS = 3600000L;
    public static final Long MINUTES = 60000L;
    public static final Long SECONDS = 1000L;
    public static final String NOT_HMS = "yyyy-MM-dd";
    public static final String YYYY = "yyyy-MM-dd HH:mm:ss";
    public static final String MM = "MM-dd HH:mm:ss";
    public static final String DD = "dd HH:mm:ss";
    public static final String HH = "HH:mm:ss";
    public static final String mm = "mm:ss";
    private static final String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

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
    public static String getDiff(Date endDate, Date startDate) {
        return getDiff(endDate, startDate, true);
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
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startDate.getTime();
        //计算差多少年
        long years = diff / YEARS;
        //计算差多少月
        long diffSeconds = diff % YEARS / MONTHS;
        // 计算差多少天
        long days = diff % YEARS % MONTHS / DAYS;
        // 计算差多少小时
        long hours = diff % DAYS / HOURS;
        // 计算差多少分钟
        long minutes = diff % DAYS % HOURS / MINUTES;
        // 计算差多少秒//输出结果
        long sec = diff % DAYS % HOURS % MINUTES / SECONDS;

        //根据相差的时间返回不同的格式
        if (years != 0) {
            if (falg) {
                return String.format("%d年 %d月 %d日 %d时 %d分 %d秒", years, diffSeconds, days, hours, minutes, sec);
                /* return years + "年" + diffSeconds + "月" + days + "日" + hours + "时" + minutes + "分" + sec + "秒";*/
            }
            return String.format("%d %d %d %d:%d:%d", years, diffSeconds, days, hours, minutes, sec);
            /*  return years + " " + diffSeconds + " " + days + " " + hours + ":" + minutes + ":" + sec;*/
        }
        if (diffSeconds != 0) {
            if (falg) {
                return String.format("%d月 %d日 %d时 %d分 %d秒", diffSeconds, days, hours, minutes, sec);
                /*return diffSeconds + "月" + days + "日" + hours + "时" + minutes + "分" + sec + "秒";*/
            }
            return String.format("%d %d %d:%d:%d", diffSeconds, days, hours, minutes, sec);
            /*  return diffSeconds + " " + days + " " + hours + ":" + minutes + ":" + sec;*/
        }
        if (days != 0) {
            if (falg) {
                return String.format("%d日 %d时 %d分 %d秒", days, hours, minutes, sec);
                /* return days + "日" + hours + "时" + minutes + "分" + sec + "秒";*/
            }
            return String.format("%d %d:%d:%d", days, hours, minutes, sec);
        }
        if (hours != 0) {
            if (falg) {
                return String.format("%d时 %d分 %d秒", hours, minutes, sec);
            }
            return String.format("%d:%d:%d", hours, minutes, sec);

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
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        return diff % DAYS / HOURS;
    }

    /**
     * 取两个时间相差的分钟
     *
     * @param endDate 结束时间
     * @param nowDate 现在的时间
     * @return 相差的分钟
     */
    public static long getDateMin(Date endDate, Date nowDate) {
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少分钟
        return diff % DAYS % HOURS / MINUTES;
    }

    /**
     * 取两个时间相差的秒
     *
     * @param endDate 结束时间
     * @param nowDate 现在的时间
     * @return 相差的秒数
     */
    public static long getDateSecond(Date endDate, Date nowDate) {
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少秒//输出结果
        return diff % DAYS % HOURS % MINUTES / SECONDS;
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

    /**
     * 增加时间
     *
     * @param old    过去的时间
     * @param now    当前时间
     * @param field  要增加的类型 如 天 时 分 秒
     * @param amount 要增加的时间
     */
    public static String getDateWeek(Date old, Date now, int field, int amount) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(old);
        calendar.add(field, amount);
        old = calendar.getTime();
        long diff = old.getTime() - now.getTime();
        // 计算差多少天
        long day = diff / DAYS;
        // 计算差多少小时
        long hour = diff % DAYS / HOURS;
        // 计算差多少分钟
        long min = diff % DAYS % HOURS / MINUTES;
        // 计算差多少秒//输出结果
        long sec = diff % DAYS % HOURS % MINUTES / SECONDS;
        if (day != 0) {
            return day + "天 " + hour + "小时 " + min + "分钟 " + sec + "秒";
        } else if (hour != 0) {
            return hour + "小时 " + min + "分钟 " + sec + "秒";
        } else if (min != 0) {
            return min + "分钟 " + sec + "秒";
        } else {
            return sec + "秒";
        }
    }

    public static String getDate() {
        return getDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }


    public static String getDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }


}

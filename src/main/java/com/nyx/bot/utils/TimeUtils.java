package com.nyx.bot.utils;

import io.github.kingprimes.utils.TimeZoneUtil;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@SuppressWarnings("unused")
public class TimeUtils {
    public static String timeDeltaToString(long millis) {
        long seconds = millis / 1000;
        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    public static long timeDeltaToMinutes(long startMillis, long endMillis, String timezone) {
        // 计算两个时区时间的分钟差（考虑时区偏移和夏令时）
        Duration duration = calculateTimeDelta(startMillis, endMillis, timezone);
        return Math.abs(duration.toMinutes());
    }

    public static long timeDeltaToMinutes(long startMillis, long endMillis) {
        return timeDeltaToMinutes(startMillis, endMillis, TimeZoneUtil.getEffectiveTimeZone());
    }

    public static long timeDeltaToMinutes(long startMillis) {
        return timeDeltaToMinutes(startMillis, System.currentTimeMillis());
    }

    /**
     * 计算两个时间戳之间的差异，并根据时区返回格式化的字符串结果
     *
     * @param startMillis 起始时间戳（毫秒）
     * @param endMillis   结束时间戳（毫秒）
     * @param timezone    时区ID
     * @return 格式化的时间差字符串
     */
    public static String timeDeltaToString(long startMillis, long endMillis, String timezone) {
        // 计算时区时间差（毫秒）并格式化
        Duration duration = calculateTimeDelta(startMillis, endMillis, timezone);
        long deltaMillis = Math.abs(duration.toMillis());

        // 使用现有的方法格式化时间差
        return timeDeltaToString(deltaMillis);
    }

    private static Duration calculateTimeDelta(long startMillis, long endMillis, String timezone) {
        if (!TimeZoneUtil.isValidTimeZone(timezone)) {
            timezone = TimeZoneUtil.getEffectiveTimeZone();
        }

        // 使用时区参数计算时间差（核心修改）
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime startZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), zoneId);
        ZonedDateTime endZoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(endMillis), zoneId);

        // 计算时区时间差（毫秒）并格式化
        return Duration.between(startZoned, endZoned);
    }

    /**
     * 计算两个时间戳之间的差异，使用系统默认时区
     *
     * @param startMillis 起始时间戳（毫秒）
     * @param endMillis   结束时间戳（毫秒）
     * @return 格式化的时间差字符串
     */
    public static String timeDeltaToString(long startMillis, long endMillis) {
        return timeDeltaToString(startMillis, endMillis, TimeZoneUtil.getEffectiveTimeZone());
    }

    /**
     * 计算指定时间戳与当前时间的差异
     *
     * @param millis   时间戳（毫秒）
     * @param timezone 时区ID
     * @return 格式化的时间差字符串
     */
    public static String timeDeltaToNow(long millis, String timezone) {
        return timeDeltaToString(millis, System.currentTimeMillis(), timezone);
    }

    /**
     * 计算指定时间戳与当前时间的差异，使用系统默认时区
     *
     * @param millis 时间戳（毫秒）
     * @return 格式化的时间差字符串
     */
    public static String timeDeltaToNow(long millis) {
        return timeDeltaToNow(millis, TimeZoneUtil.getEffectiveTimeZone());
    }
}

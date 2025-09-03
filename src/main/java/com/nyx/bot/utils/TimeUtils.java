package com.nyx.bot.utils;

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
        // 验证时区是否有效，如果无效则使用默认时区
        if (!TimeZoneUtil.isValidTimeZone(timezone)) {
            timezone = TimeZoneUtil.getEffectiveTimeZone();
        }

        // 计算时间差
        long deltaMillis = Math.abs(endMillis - startMillis);
        long seconds = deltaMillis / 1000;
        seconds %= (24 * 3600);
        seconds %= 3600;
        return seconds / 60;
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
        // 验证时区是否有效，如果无效则使用默认时区
        if (!TimeZoneUtil.isValidTimeZone(timezone)) {
            timezone = TimeZoneUtil.getEffectiveTimeZone();
        }

        // 计算时间差
        long deltaMillis = Math.abs(endMillis - startMillis);

        // 使用现有的方法格式化时间差
        return timeDeltaToString(deltaMillis);
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

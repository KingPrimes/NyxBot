package com.nyx.bot.utils;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.config.AppConfig;
import com.nyx.bot.utils.http.HttpUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
@SuppressWarnings("unused")
@Component
public class TimeZoneUtil {

    private static AppConfig appConfig;

    public TimeZoneUtil(AppConfig config) {
        appConfig = config;
    }

    // 获取系统默认时区
    public static String getSystemTimeZone() {
        return ZoneId.systemDefault().toString();
    }

    // 获取所有可用的时区ID
    public static Set<String> getAllTimeZones() {
        return ZoneId.getAvailableZoneIds();
    }

    // 格式化时间戳为指定时区的时间字符串
    public static String formatTimestamp(Long timestampMillis, String timeZone) {
        if (timestampMillis == null) {
            return null;
        }

        try {
            if (!isValidTimeZone(timeZone)) {
                timeZone = "UTC"; // 默认使用UTC
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.of(timeZone));

            return formatter.format(Instant.ofEpochMilli(timestampMillis));
        } catch (Exception e) {
            return "Invalid timestamp";
        }
    }

    public static String formatTimestamp(Long timestampMillis) {
        return formatTimestamp(timestampMillis, getEffectiveTimeZone());
    }

    // 验证时区ID是否有效
    public static boolean isValidTimeZone(String timeZone) {
        return timeZone != null && getAllTimeZones().contains(timeZone);
    }

    // 获取最终使用的时区
    public static String getEffectiveTimeZone() {
        String startupTimeZone = System.getProperty("user.timezone");
        // 1. 启动参数优先级最高
        if (isValidTimeZone(startupTimeZone)) {
            return startupTimeZone;
        }

        // 2. 从配置文件读取
        String configuredTZ = getConfiguredTimeZone();
        if (isValidTimeZone(configuredTZ)) {
            return configuredTZ;
        }

        // 3. 使用系统默认时区
        String systemTZ = getSystemTimeZone();
        if (isValidTimeZone(systemTZ)) {
            return systemTZ;
        }

        // 4. 根据IP地址获取时区
        String ipTZ = getIpTimeZone();
        if (isValidTimeZone(ipTZ)) {
            return ipTZ;
        }

        // 5. 最终回退到UTC
        return "UTC";
    }

    /**
     * 获取当前时间戳
     */
    public static Long getTimeStampMillis() {
        return System.currentTimeMillis();
    }

    private static String getConfiguredTimeZone() {
        return appConfig != null ? appConfig.getTimezone() : null;
    }

    private static String getIp() {
        HttpUtils.Body body = HttpUtils.sendGet("https://api-ipv4.ip.sb/ip");
        return body.getBody();
    }

    public static String getIpTimeZone(String ip) {
        HttpUtils.Body body = HttpUtils.sendGet("https://api.ip.sb/geoip/" + ip);
        return JSON.parseObject(body.getBody()).getString("timezone");
    }

    public static String getIpTimeZone() {
        return getIpTimeZone(getIp());
    }
}

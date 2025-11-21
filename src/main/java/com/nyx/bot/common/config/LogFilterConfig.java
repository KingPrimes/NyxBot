package com.nyx.bot.common.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 日志过滤配置类
 * 用于 WebSocket 客户端的实时过滤
 *
 * @author KinrPrimes
 */
@Data
public class LogFilterConfig {

    /**
     * 最低日志级别 (TRACE, DEBUG, INFO, WARN, ERROR)
     * 默认: INFO
     */
    private String minLevel = "INFO";

    /**
     * 关键词包含列表（包含这些词的日志会被发送）
     * 支持正则表达式（当 useRegex 为 true 时）
     */
    private List<String> includeKeywords = new ArrayList<>();

    /**
     * 关键词排除列表（包含这些词的日志不会被发送）
     * 支持正则表达式（当 useRegex 为 true 时）
     */
    private List<String> excludeKeywords = new ArrayList<>();

    /**
     * 包名白名单（只发送这些包的日志）
     * 为空表示不限制
     */
    private List<String> includePackages = new ArrayList<>();

    /**
     * 包名黑名单（不发送这些包的日志）
     */
    private List<String> excludePackages = new ArrayList<>();

    /**
     * 线程名过滤列表（只发送这些线程的日志）
     * 为空表示不限制
     */
    private List<String> includeThreads = new ArrayList<>();

    /**
     * 是否启用正则表达式匹配
     * true: 关键词作为正则表达式处理
     * false: 关键词作为普通字符串处理（大小写不敏感）
     */
    private boolean useRegex = false;

    /**
     * 是否启用过滤功能
     * true: 应用所有过滤规则
     * false: 只使用基本的级别过滤
     */
    private boolean enabled = true;

    /**
     * 创建默认配置
     *
     * @return 默认配置对象
     */
    public static LogFilterConfig createDefault() {
        return new LogFilterConfig();
    }

    /**
     * 验证配置是否有效
     *
     * @return true 如果配置有效
     */
    public boolean isValid() {
        // 验证日志级别
        return minLevel == null || isValidLevel(minLevel);
    }

    /**
     * 检查日志级别是否有效
     *
     * @param level 级别名称
     * @return true 如果有效
     */
    private boolean isValidLevel(String level) {
        return switch (level) {
            case "TRACE", "DEBUG", "INFO", "WARN", "ERROR" -> true;
            default -> false;
        };
    }

    @Override
    public String toString() {
        return String.format("LogFilterConfig{minLevel='%s', includeKeywords=%s, excludeKeywords=%s, " +
                        "includePackages=%s, excludePackages=%s, includeThreads=%s, useRegex=%s, enabled=%s}",
                minLevel, includeKeywords, excludeKeywords, includePackages, excludePackages,
                includeThreads, useRegex, enabled);
    }
}
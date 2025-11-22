package com.nyx.bot.service.log;

import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.common.config.LogFilterConfig;
import com.nyx.bot.common.event.LogEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * 日志搜索服务
 * 提供多条件日志搜索功能
 *
 * @author KinrPrimes
 */
@Slf4j
@Service
public class LogSearchService {

    @Resource
    private LogCacheManager logCacheManager;

    /**
     * 搜索日志
     *
     * @param keyword   关键词（可选，搜索日志内容和包名）
     * @param startTime 开始时间戳（可选，毫秒）
     * @param endTime   结束时间戳（可选，毫秒）
     * @param levels    日志级别列表（可选，如 ["ERROR", "WARN"]）
     * @param useRegex  是否使用正则表达式匹配关键词
     * @return 搜索结果列表
     */
    public List<LogEvent> searchLogs(
            String keyword,
            Long startTime,
            Long endTime,
            List<String> levels,
            boolean useRegex
    ) {
        // 获取所有缓存的日志
        List<LogEvent> allLogs = logCacheManager.getAllLogs();

        // 应用过滤条件
        return allLogs.stream()
                .filter(log -> matchTimeRange(log, startTime, endTime))
                .filter(log -> matchLevels(log, levels))
                .filter(log -> matchKeyword(log, keyword, useRegex))
                .collect(Collectors.toList());
    }

    /**
     * 按级别搜索日志
     *
     * @param level 日志级别
     * @return 指定级别的日志列表
     */
    public List<LogEvent> searchByLevel(String level) {
        return searchLogs(null, null, null, List.of(level), false);
    }

    /**
     * 按关键词搜索日志
     *
     * @param keyword  关键词
     * @param useRegex 是否使用正则表达式
     * @return 包含关键词的日志列表
     */
    public List<LogEvent> searchByKeyword(String keyword, boolean useRegex) {
        return searchLogs(keyword, null, null, null, useRegex);
    }

    /**
     * 按时间范围搜索日志
     *
     * @param startTime 开始时间戳（毫秒）
     * @param endTime   结束时间戳（毫秒）
     * @return 指定时间范围内的日志列表
     */
    public List<LogEvent> searchByTimeRange(long startTime, long endTime) {
        return searchLogs(null, startTime, endTime, null, false);
    }

    /**
     * 应用完整的过滤规则
     *
     * @param logEvent 日志事件
     * @param config   过滤配置
     * @return true 如果日志应该被发送
     */
    public boolean matches(LogEvent logEvent, LogFilterConfig config) {
        // 1. 级别过滤
        if (!matchLevel(logEvent, config.getMinLevel())) {
            return false;
        }

        // 2. 包名过滤
        if (!matchPackages(logEvent, config)) {
            return false;
        }

        // 3. 线程过滤
        if (!matchThreads(logEvent, config)) {
            return false;
        }

        // 4. 关键词过滤
        return matchKeywords(logEvent, config);
    }

    /**
     * 级别匹配
     *
     * @param logEvent 日志事件
     * @param minLevel 最低级别
     * @return true 如果匹配
     */
    private boolean matchLevel(LogEvent logEvent, String minLevel) {
        int logValue = getLevelValue(logEvent.getLevel());
        int minValue = getLevelValue(minLevel);
        return logValue >= minValue;
    }

    /**
     * 获取级别数值
     *
     * @param level 级别名称
     * @return 级别数值
     */
    private int getLevelValue(String level) {
        if (level == null) {
            return 2; // 默认 INFO
        }

        return switch (level.toUpperCase()) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 2;
        };
    }

    /**
     * 包名匹配
     *
     * @param logEvent 日志事件
     * @param config   过滤配置
     * @return true 如果匹配
     */
    private boolean matchPackages(LogEvent logEvent, LogFilterConfig config) {
        String pack = logEvent.getPack();

        // 黑名单检查
        if (!config.getExcludePackages().isEmpty()) {
            for (String excluded : config.getExcludePackages()) {
                if (pack.contains(excluded)) {
                    return false;
                }
            }
        }

        // 白名单检查
        if (!config.getIncludePackages().isEmpty()) {
            boolean matched = false;
            for (String included : config.getIncludePackages()) {
                if (pack.contains(included)) {
                    matched = true;
                    break;
                }
            }
            return matched;
        }

        return true;
    }

    /**
     * 线程匹配
     *
     * @param logEvent 日志事件
     * @param config   过滤配置
     * @return true 如果匹配
     */
    private boolean matchThreads(LogEvent logEvent, LogFilterConfig config) {
        if (config.getIncludeThreads().isEmpty()) {
            return true;
        }

        String thread = logEvent.getThread();
        for (String included : config.getIncludeThreads()) {
            if (thread.contains(included)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 关键词匹配
     *
     * @param logEvent 日志事件
     * @param config   过滤配置
     * @return true 如果匹配
     */
    private boolean matchKeywords(LogEvent logEvent, LogFilterConfig config) {
        String content = logEvent.getLog() + " " + logEvent.getPack();

        // 排除关键词检查
        if (!config.getExcludeKeywords().isEmpty()) {
            for (String excluded : config.getExcludeKeywords()) {
                if (matchText(content, excluded, config.isUseRegex())) {
                    return false;
                }
            }
        }

        // 包含关键词检查
        if (!config.getIncludeKeywords().isEmpty()) {
            boolean matched = false;
            for (String included : config.getIncludeKeywords()) {
                if (matchText(content, included, config.isUseRegex())) {
                    matched = true;
                    break;
                }
            }
            return matched;
        }

        return true;
    }

    /**
     * 基本级别过滤判断
     *
     * @param logLevel 日志级别
     * @param minLevel 最低级别
     * @return true 如果应该发送
     */
    public boolean shouldSendByLevel(String logLevel, String minLevel) {
        return getLevelValue(logLevel) >= getLevelValue(minLevel);
    }

    /**
     * 文本匹配（支持正则）
     *
     * @param text     文本内容
     * @param pattern  匹配模式
     * @param useRegex 是否使用正则表达式
     * @return true 如果匹配
     */
    private boolean matchText(String text, String pattern, boolean useRegex) {
        if (useRegex) {
            try {
                return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                        .matcher(text)
                        .find();
            } catch (PatternSyntaxException e) {
                log.warn("无效的正则表达式: {}", pattern);
                return false;
            }
        } else {
            return text.toLowerCase().contains(pattern.toLowerCase());
        }
    }


    /**
     * 匹配时间范围
     *
     * @param log       日志事件
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return true 如果在时间范围内
     */
    private boolean matchTimeRange(LogEvent log, Long startTime, Long endTime) {
        long logTime = log.getLogTimestamp();

        if (startTime != null && logTime < startTime) {
            return false;
        }

        return endTime == null || logTime <= endTime;
    }

    /**
     * 匹配日志级别
     *
     * @param log    日志事件
     * @param levels 级别列表（可选）
     * @return true 如果匹配级别
     */
    private boolean matchLevels(LogEvent log, List<String> levels) {
        if (levels == null || levels.isEmpty()) {
            return true;
        }

        return levels.stream()
                .anyMatch(level -> level.equalsIgnoreCase(log.getLevel()));
    }

    /**
     * 匹配关键词
     *
     * @param log      日志事件
     * @param keyword  关键词（可选）
     * @param useRegex 是否使用正则表达式
     * @return true 如果匹配关键词
     */
    private boolean matchKeyword(LogEvent log, String keyword, boolean useRegex) {
        if (keyword == null || keyword.isEmpty()) {
            return true;
        }

        // 搜索范围：日志内容 + 包名
        String searchText = log.getLog() + " " + log.getPack();

        if (useRegex) {
            try {
                Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
                return pattern.matcher(searchText).find();
            } catch (PatternSyntaxException e) {
                // 使用完整的日志记录器名称避免与参数冲突
                LogSearchService.log.warn("无效的正则表达式: {}", keyword);
                return false;
            }
        } else {
            // 普通字符串匹配（大小写不敏感）
            return searchText.toLowerCase().contains(keyword.toLowerCase());
        }
    }

    /**
     * 获取日志统计信息
     *
     * @return 统计信息字符串
     */
    public String getStatistics() {
        List<LogEvent> allLogs = logCacheManager.getAllLogs();

        long traceCount = allLogs.stream().filter(log -> "TRACE".equals(log.getLevel())).count();
        long debugCount = allLogs.stream().filter(log -> "DEBUG".equals(log.getLevel())).count();
        long infoCount = allLogs.stream().filter(log -> "INFO".equals(log.getLevel())).count();
        long warnCount = allLogs.stream().filter(log -> "WARN".equals(log.getLevel())).count();
        long errorCount = allLogs.stream().filter(log -> "ERROR".equals(log.getLevel())).count();

        return String.format("Total: %d, TRACE: %d, DEBUG: %d, INFO: %d, WARN: %d, ERROR: %d",
                allLogs.size(), traceCount, debugCount, infoCount, warnCount, errorCount);
    }
}
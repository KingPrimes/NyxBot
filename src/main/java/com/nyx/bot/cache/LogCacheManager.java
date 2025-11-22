package com.nyx.bot.cache;

import com.nyx.bot.common.event.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * 日志缓存管理器
 * 维护最近 N 条日志的循环缓冲区，用于新连接时的历史日志推送
 *
 * @author KinrPrimes
 */
@Slf4j
@Component
public class LogCacheManager {

    /**
     * 最大缓存日志数量
     */
    private static final int MAX_CACHE_SIZE = 50;

    /**
     * 日志缓存队列（线程安全的双端队列）
     */
    private final Deque<LogEvent> logCache = new LinkedBlockingDeque<>(MAX_CACHE_SIZE);

    /**
     * 添加日志到缓存
     * 如果缓存已满，自动淘汰最旧的日志
     *
     * @param event 日志事件
     */
    public synchronized void addLog(LogEvent event) {
        try {
            // 如果缓存已满，移除最旧的日志
            if (logCache.size() >= MAX_CACHE_SIZE) {
                logCache.pollFirst();
            }
            
            // 添加新日志到队列尾部
            logCache.offerLast(event);
            
        } catch (Exception e) {
            log.error("添加日志到缓存失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取最近的日志（带级别过滤）
     *
     * @param minLevel 最低日志级别
     * @return 过滤后的日志列表
     */
    public List<LogEvent> getRecentLogs(String minLevel) {
        int minValue = getLevelValue(minLevel);
        
        synchronized (this) {
            return logCache.stream()
                    .filter(log -> log.getLevelValue() >= minValue)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取所有缓存的日志（用于搜索）
     *
     * @return 所有日志列表的副本
     */
    public List<LogEvent> getAllLogs() {
        synchronized (this) {
            return new ArrayList<>(logCache);
        }
    }

    /**
     * 获取缓存的日志数量
     *
     * @return 当前缓存的日志数量
     */
    public int getCacheSize() {
        return logCache.size();
    }

    /**
     * 获取最大缓存容量
     *
     * @return 最大缓存容量
     */
    public int getMaxCacheSize() {
        return MAX_CACHE_SIZE;
    }

    /**
     * 清空日志缓存
     */
    public synchronized void clear() {
        logCache.clear();
        log.info("日志缓存已清空");
    }

    /**
     * 根据级别名称获取数值
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
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 2; // 默认 INFO
        };
    }

    /**
     * 获取缓存状态信息
     *
     * @return 状态描述字符串
     */
    public String getStatus() {
        return String.format("LogCache[size=%d, max=%d, usage=%.1f%%]",
                getCacheSize(),
                getMaxCacheSize(),
                (getCacheSize() * 100.0 / getMaxCacheSize()));
    }
}
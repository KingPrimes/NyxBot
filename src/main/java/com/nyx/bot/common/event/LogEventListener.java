package com.nyx.bot.common.event;

import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.controller.websocket.LogInfoWebSocket;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 日志事件监听器
 * 监听 LogEvent 事件，更新缓存并批量推送到 WebSocket 客户端
 *
 * @author KinrPrimes
 */
@Slf4j
@Component
public class LogEventListener {

    @Resource
    private LogCacheManager logCacheManager;

    @Resource
    private LogInfoWebSocket webSocketHandler;

    /**
     * 日志批量发送队列
     */
    private final BlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>(100);

    /**
     * 定时刷新调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "LogEventListener-Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * 批量发送阈值（条数）
     */
    private static final int BATCH_SIZE = 10;

    /**
     * 批量发送超时（毫秒）
     */
    private static final long FLUSH_INTERVAL_MS = 500;

    /**
     * 初始化定时刷新任务
     */
    @PostConstruct
    public void init() {
        // 每 500ms 检查一次队列，如果有日志就发送
        scheduler.scheduleAtFixedRate(
                this::flushLogs,
                FLUSH_INTERVAL_MS,
                FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
        log.debug("LogEventListener 初始化完成，批量策略: {}条或{}ms", BATCH_SIZE, FLUSH_INTERVAL_MS);
    }

    /**
     * 监听日志事件
     * 使用 @Async 异步处理，避免阻塞日志线程
     *
     * @param event 日志事件
     */
    @EventListener
    @Async
    public void handleLogEvent(LogEvent event) {
        try {
            // 1. 添加到缓存（用于历史日志）
            logCacheManager.addLog(event);

            // 2. 添加到批量发送队列
            if (!logQueue.offer(event, 100, TimeUnit.MILLISECONDS)) {
                log.warn("日志队列已满，丢弃日志: {}", event);
            }

            // 3. 如果队列达到批量大小，立即触发发送
            if (logQueue.size() >= BATCH_SIZE) {
                flushLogs();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("处理日志事件被中断: {}", e.getMessage());
        } catch (Exception e) {
            log.error("处理日志事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 批量发送日志到 WebSocket 客户端
     */
    private synchronized void flushLogs() {
        if (logQueue.isEmpty()) {
            return;
        }

        try {
            // 从队列中取出日志（最多 BATCH_SIZE 条）
            List<LogEvent> batch = new ArrayList<>();
            logQueue.drainTo(batch, BATCH_SIZE);

            if (!batch.isEmpty()) {
                // 广播到所有 WebSocket 客户端
                webSocketHandler.broadcastLogs(batch);
                //log.debug("批量发送 {} 条日志到 WebSocket 客户端", batch.size());
            }

        } catch (Exception e) {
            log.error("批量发送日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取当前队列大小
     *
     * @return 队列中的日志数量
     */
    public int getQueueSize() {
        return logQueue.size();
    }

    /**
     * 清空队列
     */
    public void clearQueue() {
        logQueue.clear();
        log.info("日志发送队列已清空");
    }

    /**
     * 容器销毁时清理资源
     */
    @PreDestroy
    public void destroy() {
        log.info("LogEventListener 正在关闭...");
        
        // 发送剩余的日志
        flushLogs();
        
        // 关闭调度器
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("LogEventListener 已关闭");
    }
}
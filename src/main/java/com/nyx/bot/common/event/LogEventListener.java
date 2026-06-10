package com.nyx.bot.common.event;

import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.controller.sse.LogSseController;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 日志事件监听器
 * 监听 LogEvent 事件，更新缓存并批量推送到 SSE 客户端
 *
 * @author KinrPrimes
 */
@Slf4j
@Component
public class LogEventListener {

    private static final int BATCH_SIZE = 10;
    private static final long FLUSH_INTERVAL_MS = 500;

    private final BlockingQueue<LogEvent> logQueue = new LinkedBlockingQueue<>(100);
    private final ReentrantLock flushLock = new ReentrantLock();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
            Thread.ofVirtual().name("LogEventListener-Scheduler").factory());

    private final LogCacheManager logCacheManager;
    private final LogSseController logSseController;

    public LogEventListener(LogCacheManager logCacheManager, LogSseController logSseController) {
        this.logCacheManager = logCacheManager;
        this.logSseController = logSseController;

        scheduler.scheduleAtFixedRate(
                this::flushLogs,
                FLUSH_INTERVAL_MS,
                FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
        );
        log.debug("LogEventListener 初始化完成，批量策略: {}条或{}ms", BATCH_SIZE, FLUSH_INTERVAL_MS);
    }

    @EventListener
    @Async
    public void handleLogEvent(LogEvent event) {
        try {
            logCacheManager.addLog(event);

            if (!logQueue.offer(event, 100, TimeUnit.MILLISECONDS)) {
                log.warn("日志队列已满，丢弃日志: {}", event);
            }

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

    private void flushLogs() {
        List<LogEvent> batch = new ArrayList<>();
        flushLock.lock();
        try {
            logQueue.drainTo(batch, BATCH_SIZE);
        } finally {
            flushLock.unlock();
        }

        if (!batch.isEmpty()) {
            try {
                logSseController.broadcastLogs(batch);
            } catch (Exception e) {
                log.error("批量发送日志失败: {}", e.getMessage(), e);
            }
        }
    }

    public int getQueueSize() {
        return logQueue.size();
    }

    public void clearQueue() {
        logQueue.clear();
        log.info("日志发送队列已清空");
    }

    @PreDestroy
    public void destroy() {
        log.info("LogEventListener 正在关闭...");

        scheduler.shutdown();
        logSseController.shutdown();
        logQueue.clear();

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

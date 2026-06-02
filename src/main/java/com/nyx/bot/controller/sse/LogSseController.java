package com.nyx.bot.controller.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.common.config.LogFilterConfig;
import com.nyx.bot.common.core.ApiResponse;
import com.nyx.bot.common.core.dao.LogInfoWebSocketDto;
import com.nyx.bot.common.event.DataRefreshEvent;
import com.nyx.bot.common.event.DownloadProgressEvent;
import com.nyx.bot.common.event.LogEvent;
import com.nyx.bot.service.log.LogInfoMapper;
import com.nyx.bot.service.log.LogSearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SSE 日志推送控制器（替代 WebSocket 方案）
 * 单向推送日志和下载进度到浏览器，连接管理由浏览器 EventSource 内置处理
 *
 * @author KingPrimes
 */
@Slf4j
@RestController
@RequestMapping("/sse")
public class LogSseController {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, String> levelFilterMap = new ConcurrentHashMap<>();
    private final Map<String, LogFilterConfig> filterConfigMap = new ConcurrentHashMap<>();
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private LogSearchService logSearchService;
    @Resource
    private LogInfoMapper logInfoMapper;
    @Resource
    private LogCacheManager logCacheManager;

    // ══════════════════════════════════════════════
    // SSE 订阅端点
    // ══════════════════════════════════════════════

    /**
     * 订阅日志流
     *
     * @param level 最低日志级别（默认 INFO）
     * @return SseEmitter 实例
     */
    @GetMapping(value = "/log-now", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam(defaultValue = "INFO") String level) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        String sessionId = UUID.randomUUID().toString();

        emitters.put(sessionId, emitter);
        levelFilterMap.put(sessionId, level.toUpperCase());
        filterConfigMap.put(sessionId, LogFilterConfig.createDefault());
        log.debug("SSE 连接建立: sessionId={}, level={}", sessionId, level);

        // 首先发送 sessionId 给客户端，以便后续调用过滤接口时使用
        try {
            emitter.send(SseEmitter.event()
                    .name("session")
                    .data(objectMapper.writeValueAsString(Map.of("sessionId", sessionId)),
                            MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.error("发送 sessionId 失败: sessionId={}", sessionId, e);
            removeEmitter(sessionId);
            return emitter;
        }

        // 连接建立时发送历史日志
        sendHistoryLogs(sessionId, emitter, level.toUpperCase());

        // 清理回调
        emitter.onCompletion(() -> removeEmitter(sessionId, true));
        emitter.onTimeout(() -> removeEmitter(sessionId, true));
        emitter.onError(e -> removeEmitter(sessionId, false));

        return emitter;
    }

    /**
     * 订阅数据刷新事件流
     * 前端在用户点击"更新数据"按钮后连接此端点，接收刷新进度通知
     */
    @GetMapping(value = "/data-refresh", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeDataRefresh() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        String sessionId = UUID.randomUUID().toString();

        emitters.put(sessionId, emitter);
        log.debug("数据刷新 SSE 连接建立: sessionId={}", sessionId);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(objectMapper.writeValueAsString(Map.of("sessionId", sessionId)),
                            MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.error("发送数据刷新 sessionId 失败: sessionId={}", sessionId, e);
            removeEmitter(sessionId);
            return emitter;
        }

        emitter.onCompletion(() -> removeEmitter(sessionId, true));
        emitter.onTimeout(() -> removeEmitter(sessionId, true));
        emitter.onError(e -> removeEmitter(sessionId, false));

        return emitter;
    }

    private void removeEmitter(String sessionId) {
        removeEmitter(sessionId, true);
    }

    private void removeEmitter(String sessionId, boolean complete) {
        SseEmitter emitter = emitters.remove(sessionId);
        levelFilterMap.remove(sessionId);
        filterConfigMap.remove(sessionId);
        if (emitter != null && complete) {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // 响应已不可用
            }
        }
    }

    // ══════════════════════════════════════════════
    // 日志广播（由 LogEventListener 调用）
    // ══════════════════════════════════════════════

    /**
     * 批量推送日志到所有 SSE 客户端
     */
    public void broadcastLogs(List<LogEvent> logs) {
        if (logs == null || logs.isEmpty()) return;

        emitters.forEach((sessionId, emitter) -> {
            try {
                LogFilterConfig config = filterConfigMap.getOrDefault(sessionId, LogFilterConfig.createDefault());
                String minLevel = levelFilterMap.getOrDefault(sessionId, "INFO");

                List<LogInfoWebSocketDto> filtered = logs.stream()
                        .filter(e -> config.isEnabled()
                                ? logSearchService.matches(e, config)
                                : logSearchService.shouldSendByLevel(e.getLevel(), minLevel))
                        .map(logInfoMapper::toDto)
                        .collect(Collectors.toList());

                if (!filtered.isEmpty()) {
                    emitter.send(SseEmitter.event()
                            .name("log")
                            .data(objectMapper.writeValueAsString(filtered), MediaType.APPLICATION_JSON));
                }
            } catch (Exception e) {
                log.debug("SSE 推送日志失败，移除连接: sessionId={}", sessionId);
                removeEmitter(sessionId);
            }
        });
    }

    // ══════════════════════════════════════════════
    // 下载进度事件监听
    // ══════════════════════════════════════════════

    /**
     * 监听下载进度事件并推送到所有 SSE 客户端
     */
    @EventListener
    @Async("taskExecutor")
    @SuppressWarnings("unused")
    public void onDownloadProgress(DownloadProgressEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "progress");
        data.put("url", event.getUrl());
        data.put("downloaded", event.getDownloaded());
        data.put("total", event.getTotal());
        data.put("done", event.isDone());
        if (event.getTotal() > 0) {
            data.put("percent", Math.min(100, (double) event.getDownloaded() / event.getTotal() * 100));
        }

        emitters.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(objectMapper.writeValueAsString(data), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                log.debug("SSE 推送进度失败，移除连接: sessionId={}", sessionId);
                removeEmitter(sessionId);
            }
        });
    }

    /**
     * 监听数据刷新事件并推送到所有 SSE 客户端
     */
    @EventListener
    @Async("taskExecutor")
    @SuppressWarnings("unused")
    public void onDataRefresh(DataRefreshEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("type", "dataRefresh");
        data.put("taskName", event.getTaskName());
        data.put("status", event.getStatus().name());
        data.put("message", event.resolveMessage());

        emitters.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("data-refresh")
                        .data(objectMapper.writeValueAsString(data), MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                log.debug("SSE 推送数据刷新状态失败，移除连接: sessionId={}", sessionId);
                removeEmitter(sessionId);
            }
        });
    }

    /**
     * 关闭所有 SSE 连接（容器销毁时调用）
     */
    public void shutdown() {
        log.info("正在关闭所有 SSE 连接，活跃数: {}", emitters.size());
        emitters.keySet().forEach(this::removeEmitter);
        log.info("所有 SSE 连接已关闭");
    }

    // ══════════════════════════════════════════════
    // 过滤配置 REST 接口
    // ══════════════════════════════════════════════

    /**
     * 更新过滤配置（通过 sessionId 定位客户端）
     */
    @PostMapping("/filter/update")
    public ApiResponse<?> updateFilter(@RequestParam String sessionId, @RequestBody LogFilterConfig config) {
        if (!emitters.containsKey(sessionId)) {
            return ApiResponse.error(400, "会话不存在");
        }
        if (!config.isValid()) {
            return ApiResponse.error(400, "无效的过滤配置");
        }
        filterConfigMap.put(sessionId, config);
        log.info("SSE 会话 {} 更新过滤配置", sessionId);
        return ApiResponse.ok("过滤配置已更新", Map.of("config", config));
    }

    /**
     * 重置过滤配置
     */
    @PostMapping("/filter/reset")
    public ApiResponse<?> resetFilter(@RequestParam String sessionId) {
        if (!emitters.containsKey(sessionId)) {
            return ApiResponse.error(400, "会话不存在");
        }
        LogFilterConfig defaultConfig = LogFilterConfig.createDefault();
        String minLevel = levelFilterMap.getOrDefault(sessionId, "INFO");
        defaultConfig.setMinLevel(minLevel);
        filterConfigMap.put(sessionId, defaultConfig);
        return ApiResponse.ok("过滤配置已重置", Map.of("config", (Object) defaultConfig));
    }

    /**
     * 获取当前活跃的 SSE 连接数
     */
    @GetMapping("/stats")
    public ApiResponse<?> stats() {
        return ApiResponse.ok(Map.of("activeConnections", emitters.size(), "protocol", "SSE (Server-Sent Events)"));
    }

    // ══════════════════════════════════════════════
    // 历史日志
    // ══════════════════════════════════════════════

    private void sendHistoryLogs(String sessionId, SseEmitter emitter, String minLevel) {
        try {
            List<LogEvent> historyLogs = logCacheManager.getRecentLogs(minLevel);
            if (!historyLogs.isEmpty()) {
                List<LogInfoWebSocketDto> dtoList = historyLogs.stream()
                        .map(logInfoMapper::toDto)
                        .collect(Collectors.toList());
                emitter.send(SseEmitter.event()
                        .name("history")
                        .data(objectMapper.writeValueAsString(dtoList), MediaType.APPLICATION_JSON));
            }
        } catch (Exception e) {
            log.error("发送历史日志失败: sessionId={}", sessionId, e);
        }
    }
}

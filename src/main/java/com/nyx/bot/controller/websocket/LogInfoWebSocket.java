package com.nyx.bot.controller.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.common.config.LogFilterConfig;
import com.nyx.bot.common.core.dao.LogInfoWebSocketDto;
import com.nyx.bot.common.core.helper.WebSocketMessageSender;
import com.nyx.bot.common.event.LogEvent;
import com.nyx.bot.service.log.LogInfoMapper;
import com.nyx.bot.service.log.LogSearchService;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.SpringUtils;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket 日志推送端点
 * 实时推送日志到客户端，支持级别过滤和实时过滤配置
 * 所有数据始终使用 GZIP 压缩传输
 *
 * @author KinrPrimes
 */
@Slf4j
@Component
@ServerEndpoint(value = "/ws/log-now", configurator = LogInfoWebSocket.CustomConfigurator.class)
public class LogInfoWebSocket {

    /**
     * 所有活动的 WebSocket 会话
     */
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    /**
     * 每个会话的日志级别过滤配置（用于基本级别过滤）
     */
    private static final Map<String, String> levelFilterMap = new ConcurrentHashMap<>();
    /**
     * 每个会话的完整过滤配置（用于高级过滤）
     */
    private static final Map<String, LogFilterConfig> filterConfigMap = new ConcurrentHashMap<>();
    private static LogInfoMapper logInfoMapper;
    private static ObjectMapper objectMapper;
    private static LogSearchService logSearchService;
    private static WebSocketMessageSender webSocketMessageSender;

    @Resource
    public void setLogInfoMapper(LogInfoMapper logInfoMapper) {
        LogInfoWebSocket.logInfoMapper = logInfoMapper;
    }

    @Resource
    public void setObjectMapper(ObjectMapper objectMapper) {
        LogInfoWebSocket.objectMapper = objectMapper;
    }

    @Resource
    public void setLogSearchService(LogSearchService logSearchService) {
        LogInfoWebSocket.logSearchService = logSearchService;
    }

    @Resource
    public void setWebSocketMessageSender(WebSocketMessageSender webSocketMessageSender) {
        LogInfoWebSocket.webSocketMessageSender = webSocketMessageSender;
    }

    /**
     * 移除会话以及相关的过滤配置，避免资源泄漏
     */
    private static void removeSession(Session session) {
        if (session == null) {
            return;
        }
        removeSession(session.getId());
    }

    private static void removeSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        sessionMap.remove(sessionId);
        levelFilterMap.remove(sessionId);
        filterConfigMap.remove(sessionId);
    }

    /**
     * 连接建立成功调用的方法
     *
     * @param session WebSocket 会话
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            // 1. 解析连接参数（日志级别）
            String level = getQueryParam(session);
            String logLevel = (level != null && !level.isEmpty()) ? level.toUpperCase() : "INFO";

            // 2. 保存会话和配置
            sessionMap.put(session.getId(), session);
            levelFilterMap.put(session.getId(), logLevel);

            // 3. 初始化默认的过滤配置
            LogFilterConfig defaultConfig = LogFilterConfig.createDefault();
            defaultConfig.setMinLevel(logLevel);
            filterConfigMap.put(session.getId(), defaultConfig);

            log.debug("WebSocket 连接建立: sessionId={}, level={} (压缩已启用)",
                    session.getId(), logLevel);

            // 4. 发送历史日志
            sendHistoryLogs(session, logLevel);

        } catch (Exception e) {
            log.error("WebSocket 连接建立失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 接收客户端消息
     *
     * @param session WebSocket 会话
     * @param message 客户端消息
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        try {
            JsonNode messageNode = objectMapper.readTree(message);
            String type = messageNode.get("type").asText();
            if (type == null) {
                sendError(session, "缺少消息类型字段: type");
                return;
            }

            switch (type) {
                case "filter" -> handleFilterMessage(session, messageNode);
                case "ping" -> handlePingMessage(session);
                case "request" -> handleRequestMessage(session, messageNode);
                default -> sendError(session, "未知的消息类型: " + type);
            }
        } catch (Exception e) {
            log.error("处理客户端消息失败: {}", e.getMessage(), e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 连接关闭调用的方法
     *
     * @param session WebSocket 会话
     */
    @OnClose
    public void onClose(Session session) {
        removeSession(session);
        log.debug("WebSocket 连接关闭: sessionId={}", session.getId());
    }

    /**
     * 发生错误时调用
     *
     * @param session WebSocket 会话
     * @param error   错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 错误: sessionId={}, message={}",
                session != null ? session.getId() : "unknown",
                error.getMessage());
    }

    /**
     * 广播日志到所有客户端（应用过滤配置）
     * 此方法由 LogEventListener 调用
     *
     * @param logs 日志列表
     */
    public void broadcastLogs(List<LogEvent> logs) {
        sessionMap.forEach((sessionId, session) -> {
            try {
                // 检查会话是否已经关闭，如果关闭则移除它
                if (!session.isOpen()) {
                    removeSession(sessionId);
                    return;
                }

                List<LogInfoWebSocketDto> filtered = filterLogsForSession(sessionId, logs);

                if (!filtered.isEmpty()) {
                    String json = objectMapper.writeValueAsString(filtered);
                    send(session, json);
                }
            } catch (Exception e) {
                log.error("广播日志到会话 {} 失败: {}", sessionId, e.getMessage());
            }
        });
    }

    /**
     * 根据会话的过滤配置筛选日志
     *
     * @param sessionId 会话ID
     * @param logs      待筛选的日志列表
     * @return 筛选后的日志DTO列表
     */
    private List<LogInfoWebSocketDto> filterLogsForSession(String sessionId, List<LogEvent> logs) {
        LogFilterConfig config = filterConfigMap.getOrDefault(
                sessionId,
                LogFilterConfig.createDefault()
        );

        if (!config.isEnabled()) {
            String minLevel = levelFilterMap.getOrDefault(sessionId, "INFO");
            return logs.stream()
                    .filter(logEvent -> logSearchService.shouldSendByLevel(logEvent.getLevel(), minLevel))
                    .map(logInfoMapper::toDto)
                    .collect(Collectors.toList());
        }

        return logs.stream()
                .filter(logEvent -> logSearchService.matches(logEvent, config))
                .map(logInfoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 发送历史日志到新连接的客户端
     *
     * @param session  WebSocket 会话
     * @param minLevel 最低日志级别
     */
    private void sendHistoryLogs(Session session, String minLevel) {
        try {
            LogCacheManager cacheManager = SpringUtils.getBean(LogCacheManager.class);

            List<LogEvent> historyLogs = cacheManager.getRecentLogs(minLevel);

            if (!historyLogs.isEmpty()) {
                List<LogInfoWebSocketDto> dtoList = historyLogs.stream()
                        .map(logInfoMapper::toDto)
                        .collect(Collectors.toList());

                String json = objectMapper.writeValueAsString(dtoList);
                send(session, json);

                //log.info("发送 {} 条历史日志到会话: {}", dtoList.size(), session.getId());
            }
        } catch (Exception e) {
            log.error("发送历史日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理过滤配置消息
     *
     * @param session     WebSocket 会话
     * @param messageNode 消息节点
     */
    private void handleFilterMessage(Session session, JsonNode messageNode) throws Exception {
        String action = messageNode.get("action").asText();

        switch (action) {
            case "update" -> updateFilterConfig(session, messageNode.get("config"));
            case "reset" -> resetFilterConfig(session);
            case "get" -> sendCurrentConfig(session);
            default -> sendError(session, "未知的过滤操作: " + action);
        }
    }

    /**
     * 更新过滤配置
     *
     * @param session    WebSocket 会话
     * @param configNode 配置节点
     */
    private void updateFilterConfig(Session session, JsonNode configNode) throws Exception {
        LogFilterConfig config = objectMapper.treeToValue(configNode, LogFilterConfig.class);

        // 验证配置
        if (!config.isValid()) {
            sendError(session, "无效的过滤配置");
            return;
        }

        // 保存配置
        filterConfigMap.put(session.getId(), config);

        // 发送确认消息
        Map<String, Object> response = new HashMap<>();
        response.put("type", "filter_response");
        response.put("status", "success");
        response.put("message", "过滤配置已更新");
        response.put("config", config);
        send(session, objectMapper.writeValueAsString(response));

        log.info("会话 {} 更新过滤配置: {}", session.getId(), config);
    }

    /**
     * 重置过滤配置
     *
     * @param session WebSocket 会话
     */
    private void resetFilterConfig(Session session) throws JsonProcessingException {
        LogFilterConfig defaultConfig = LogFilterConfig.createDefault();
        String minLevel = levelFilterMap.getOrDefault(session.getId(), "INFO");
        defaultConfig.setMinLevel(minLevel);

        filterConfigMap.put(session.getId(), defaultConfig);

        Map<String, Object> response = new HashMap<>();
        response.put("type", "filter_response");
        response.put("status", "success");
        response.put("message", "过滤配置已重置");
        response.put("config", defaultConfig);


        send(session, objectMapper.writeValueAsString(response));
        log.info("会话 {} 重置过滤配置: {}", session.getId(), defaultConfig);
    }

    /**
     * 发送当前配置
     *
     * @param session WebSocket 会话
     */
    private void sendCurrentConfig(Session session) throws JsonProcessingException {
        LogFilterConfig config = filterConfigMap.getOrDefault(
                session.getId(),
                LogFilterConfig.createDefault()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("type", "filter_response");
        response.put("status", "success");
        response.put("config", config);
        send(session, objectMapper.writeValueAsString(response));
    }

    /**
     * 处理心跳消息
     *
     * @param session WebSocket 会话
     */
    private void handlePingMessage(Session session) throws JsonProcessingException {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "pong");
        response.put("timestamp", System.currentTimeMillis());

        send(session, objectMapper.writeValueAsString(response));
    }

    /**
     * 处理请求消息（预留接口）
     *
     * @param session     WebSocket 会话
     * @param messageNode 消息节点
     */
    private void handleRequestMessage(Session session, JsonNode messageNode) {
        String requestType = messageNode.has("requestType") ?
                messageNode.get("requestType").asText() : "unknown";

        if (requestType.equals("history")) {// 重新发送历史日志
            String level = levelFilterMap.getOrDefault(session.getId(), "INFO");
            sendHistoryLogs(session, level);
        } else {
            sendError(session, "未知的请求类型: " + requestType);
        }
    }

    /**
     * 发送错误消息
     *
     * @param session      WebSocket 会话
     * @param errorMessage 错误消息
     */
    private void sendError(Session session, String errorMessage) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "error");
            response.put("message", errorMessage);
            response.put("timestamp", System.currentTimeMillis());
            send(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("发送错误消息失败: {}", e.getMessage());
        }
    }


    /**
     * 获取查询参数
     *
     * @param session WebSocket 会话
     * @return 参数值
     */
    private String getQueryParam(Session session) {
        String queryString = session.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        for (String param : queryString.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equalsIgnoreCase("level")) {
                String raw = pair[1];
                try {
                    return URLDecoder.decode(raw, StandardCharsets.UTF_8);
                } catch (IllegalArgumentException | NullPointerException e) {
                    return raw;
                }
            }
        }

        return null;
    }

    /**
     * 发送消息到客户端（始终使用 GZIP 压缩）
     *
     * @param session WebSocket 会话
     * @param message 消息内容
     */
    private void send(Session session, String message) {
        if (!session.isOpen()) {
            removeSession(session);
            return;
        }
        try {
            webSocketMessageSender.sendCompressed(session, message);
        } catch (IOException e) {
            removeSession(session);
            log.error("发送消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * WebSocket 自定义配置器
     */
    public static class CustomConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            Object o = CacheUtils.get(CacheUtils.SYSTEM, "sec-websocket-protocol");
            String protocol = "";
            if (o != null) {
                protocol = o.toString();
            }
            // 设置自定义的配置
            response.getHeaders().put("Sec-WebSocket-Protocol", Collections.singletonList(protocol));
            super.modifyHandshake(sec, request, response);
        }
    }
}

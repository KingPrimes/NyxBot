package com.nyx.bot.controller.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.cache.LogCacheManager;
import com.nyx.bot.common.config.LogFilterConfig;
import com.nyx.bot.common.event.LogEvent;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.SpringUtils;
import jakarta.websocket.*;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

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

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

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


    /**
     * 连接建立成功调用的方法
     *
     * @param session WebSocket 会话
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            // 1. 解析连接参数（日志级别）
            String level = getQueryParam(session, "level");
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

            switch (type) {
                case "filter":
                    handleFilterMessage(session, messageNode);
                    break;
                case "ping":
                    handlePingMessage(session);
                    break;
                case "request":
                    handleRequestMessage(session, messageNode);
                    break;
                default:
                    sendError(session, "未知的消息类型: " + type);
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
        sessionMap.remove(session.getId());
        levelFilterMap.remove(session.getId());
        filterConfigMap.remove(session.getId());
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
                // 获取该会话的过滤配置
                final LogFilterConfig config = filterConfigMap.getOrDefault(
                        sessionId,
                        LogFilterConfig.createDefault()
                );

                // 如果过滤未启用，使用基本的级别过滤
                List<LogInfoWebSocketForStr> filtered;
                if (!config.isEnabled()) {
                    String minLevel = levelFilterMap.getOrDefault(sessionId, "INFO");
                    filtered = logs.stream()
                            .filter(logEvent -> shouldSendByLevel(logEvent.getLevel(), minLevel))
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                } else {
                    // 应用完整的过滤配置
                    filtered = logs.stream()
                            .filter(logEvent -> applyFilter(logEvent, config))
                            .map(this::convertToDto)
                            .collect(Collectors.toList());
                }

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
                List<LogInfoWebSocketForStr> dtoList = historyLogs.stream()
                        .map(this::convertToDto)
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
            case "update":
                updateFilterConfig(session, messageNode.get("config"));
                break;
            case "reset":
                resetFilterConfig(session);
                break;
            case "get":
                sendCurrentConfig(session);
                break;
            default:
                sendError(session, "未知的过滤操作: " + action);
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
    private void resetFilterConfig(Session session) throws Exception {
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
    }

    /**
     * 发送当前配置
     *
     * @param session WebSocket 会话
     */
    private void sendCurrentConfig(Session session) throws Exception {
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
    private void handlePingMessage(Session session) throws Exception {
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
     * 应用完整的过滤规则
     *
     * @param logEvent 日志事件
     * @param config   过滤配置
     * @return true 如果日志应该被发送
     */
    private boolean applyFilter(LogEvent logEvent, LogFilterConfig config) {
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
     * 基本级别过滤判断
     *
     * @param logLevel 日志级别
     * @param minLevel 最低级别
     * @return true 如果应该发送
     */
    private boolean shouldSendByLevel(String logLevel, String minLevel) {
        int logValue = getLevelValue(logLevel);
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
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 2;
        };
    }

    /**
     * 转换 LogEvent 到 DTO
     *
     * @param event 日志事件
     * @return DTO 对象
     */
    private LogInfoWebSocketForStr convertToDto(LogEvent event) {
        LogInfoWebSocketForStr dto = new LogInfoWebSocketForStr();
        dto.setLive(event.getLevel());
        dto.setTime(event.getTime());
        dto.setThread(event.getThread());
        dto.setPack(event.getPack());
        dto.setLog(event.getLog());
        return dto;
    }

    /**
     * 获取查询参数
     *
     * @param session WebSocket 会话
     * @param name    参数名
     * @return 参数值
     */
    private String getQueryParam(Session session, String name) {
        String queryString = session.getQueryString();
        if (queryString == null || queryString.isEmpty()) {
            return null;
        }

        for (String param : queryString.split("&")) {
            String[] pair = param.split("=");
            if (pair.length == 2 && pair[0].equals(name)) {
                return pair[1];
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
        try {
            if (!session.isOpen()) {
                return;
            }

            // 始终使用 GZIP 压缩
            byte[] compressed = compressData(message);
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(compressed));
        } catch (Exception e) {
            log.error("WebSocket 发送消息失败: {}", e.getMessage());
        }
    }

    /**
     * 压缩数据
     *
     * @param data 原始数据
     * @return 压缩后的数据
     */
    private byte[] compressData(String data) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(bos)) {
            gzipOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
        }
        return bos.toByteArray();
    }


    /**
     * 日志数据传输对象
     */
    @Data
    public static class LogInfoWebSocketForStr {
        String live;
        String time;
        String thread;
        String pack;
        String log;

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("live", live)
                    .append("time", time)
                    .append("thread", thread)
                    .append("pack", pack)
                    .append("log", log)
                    .toString();
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

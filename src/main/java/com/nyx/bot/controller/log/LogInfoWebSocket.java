package com.nyx.bot.controller.log;

import com.alibaba.fastjson2.JSON;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@ServerEndpoint("/ws/log")
public class LogInfoWebSocket {

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> lengthMap = new ConcurrentHashMap<>();
    //用于匹配日志格式的正则表达式
    private static final String LOG_PATTERN = "(\\w+)\\s+(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+\\[(.*?)\\]\\s+(.*?)\\s*:\\s*(.*)";

    public static LogInfoWebSocketForStr parseLogLine(String logLine) {
        Pattern pattern = Pattern.compile(LOG_PATTERN);
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.matches()) {
            LogInfoWebSocketForStr logEntry = new LogInfoWebSocketForStr();
            logEntry.setLive(matcher.group(1));
            logEntry.setTime(matcher.group(2));
            logEntry.setThread(matcher.group(3));
            logEntry.setPack(matcher.group(4));
            logEntry.setLog(matcher.group(5));
            return logEntry;
        }
        return null;
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        //添加到集合中
        sessionMap.put(session.getId(), session);
        lengthMap.put(session.getId(), 1);//默认从第一行开始

        //获取日志信息
        new Thread(() -> {
            while (sessionMap.get(session.getId()) != null) {
                //日志文件路径，获取最新的
                String filePath = "./logs/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    Object[] lines = reader.lines().toArray();

                    //只取从上次之后产生的日志
                    Object[] copyOfRange = Arrays.copyOfRange(lines, lengthMap.get(session.getId()), lines.length);
                    List<LogInfoWebSocketForStr> logInfoWebSocketForStrList = new ArrayList<>();
                    // 对日志进行封装
                    for (Object o : copyOfRange) {
                        String line = (String) o;
                        LogInfoWebSocketForStr logInfoWebSocketForStr = parseLogLine(line);
                        if (logInfoWebSocketForStr != null) {
                            logInfoWebSocketForStrList.add(logInfoWebSocketForStr);
                        }
                    }
                    //存储最新一行开始
                    lengthMap.put(session.getId(), lines.length);

                    if (!logInfoWebSocketForStrList.isEmpty()) {
                        //发送 Json格式的日志到前端
                        send(session, JSON.toJSONString(logInfoWebSocketForStrList));
                    }
                    //休眠一秒
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //捕获但不处理
                    log.error(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        //从集合中删除
        sessionMap.remove(session.getId());
        lengthMap.remove(session.getId());
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("Session:{},message:{}", session, error.getMessage());
    }

    /**
     * 封装一个send方法，发送消息到前端
     */
    private void send(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("WebSocket Send Error:{}", e.getMessage());
        }
    }

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
}

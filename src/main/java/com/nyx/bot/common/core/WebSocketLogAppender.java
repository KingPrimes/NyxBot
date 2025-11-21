package com.nyx.bot.common.core;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.nyx.bot.common.event.LogEvent;
import com.nyx.bot.utils.SpringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 自定义 Logback Appender
 * 拦截所有日志事件并通过 Spring 事件总线广播到 WebSocket
 *
 * @author KinrPrimes
 */
public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {

    /**
     * 时间格式化器（线程安全）
     */
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * Logback 调用此方法处理每条日志
     *
     * @param event 日志事件
     */
    @Override
    protected void append(ILoggingEvent event) {
        try {
            // 检查 ApplicationContext 是否已初始化
            if (!SpringUtils.isInitialized()) {
                return;
            }

            // 1. 提取日志信息
            String level = event.getLevel().toString();
            String time = formatTime(event.getTimeStamp());
            String thread = event.getThreadName();
            String logger = event.getLoggerName();
            String message = event.getFormattedMessage();
            long timestamp = event.getTimeStamp();

            // 2. 创建 LogEvent 对象
            LogEvent logEvent = new LogEvent(
                    this,
                    level,
                    time,
                    thread,
                    logger,
                    message,
                    timestamp
            );

            // 3. 通过 Spring 事件总线发布事件
            SpringUtils.publishEvent(logEvent);

        } catch (Exception e) {
            // 不影响原日志系统，静默处理异常
            addError("Failed to publish log event", e);
        }
    }

    /**
     * 格式化时间戳
     *
     * @param timestamp 时间戳（毫秒）
     * @return 格式化的时间字符串
     */
    private String formatTime(long timestamp) {
        return DATE_FORMAT.get().format(new Date(timestamp));
    }

    @Override
    public void start() {
        super.start();
        addInfo("WebSocketLogAppender started");
    }

    @Override
    public void stop() {
        super.stop();
        addInfo("WebSocketLogAppender stopped");
    }
}
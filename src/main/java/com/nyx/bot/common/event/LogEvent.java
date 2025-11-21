package com.nyx.bot.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 日志事件类
 * 用于在 Spring 事件总线中传递日志信息
 *
 * @author KinrPrimes
 */
@Getter
public class LogEvent extends ApplicationEvent {

    /**
     * 日志级别 (TRACE, DEBUG, INFO, WARN, ERROR)
     */
    private final String level;

    /**
     * 格式化的时间字符串 (yyyy-MM-dd HH:mm:ss)
     */
    private final String time;

    /**
     * 线程名
     */
    private final String thread;

    /**
     * 包名/类名
     */
    private final String pack;

    /**
     * 日志内容
     */
    private final String log;

    /**
     * 日志时间戳 (用于时间范围搜索)
     */
    private final long logTimestamp;

    /**
     * 日志级别数值 (用于级别比较)
     * TRACE=0, DEBUG=1, INFO=2, WARN=3, ERROR=4
     */
    private final int levelValue;

    /**
     * 构造函数
     *
     * @param source       事件源
     * @param level        日志级别
     * @param time         格式化时间
     * @param thread       线程名
     * @param pack         包名/类名
     * @param log          日志内容
     * @param logTimestamp 原始时间戳
     */
    public LogEvent(Object source, String level, String time, String thread, String pack, String log, long logTimestamp) {
        super(source);
        this.level = level;
        this.time = time;
        this.thread = thread;
        this.pack = pack;
        this.log = log;
        this.logTimestamp = logTimestamp;
        this.levelValue = getLevelValueByName(level);
    }

    /**
     * 根据级别名称获取数值
     *
     * @param level 级别名称
     * @return 级别数值
     */
    private int getLevelValueByName(String level) {
        return switch (level) {
            case "TRACE" -> 0;
            case "DEBUG" -> 1;
            case "INFO" -> 2;
            case "WARN" -> 3;
            case "ERROR" -> 4;
            default -> 2; // 默认 INFO
        };
    }

    @Override
    public String toString() {
        return String.format("%s %s [%s] %s : %s", level, time, thread, pack, log);
    }
}
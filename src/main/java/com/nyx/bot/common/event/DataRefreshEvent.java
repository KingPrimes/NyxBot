package com.nyx.bot.common.event;

import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * 数据刷新状态事件
 * 由数据刷新任务发布，供 SSE 推送刷新进度到前端
 */
@Getter
public class DataRefreshEvent extends ApplicationEvent {

    private final String taskName;
    private final Status status;
    private final String messageKey;
    private final Object[] args;
    private final Locale locale;

    public DataRefreshEvent(Object source, String taskName, Status status, String messageKey, Object[] args, Locale locale) {
        super(source);
        this.taskName = taskName;
        this.status = status;
        this.messageKey = messageKey;
        this.args = args;
        this.locale = locale;
    }

    /**
     * 异步执行数据刷新任务并发布状态事件
     *
     * @param publisher   事件发布器
     * @param taskNameKey 任务名称 i18n key
     * @param task        刷新逻辑
     */
    public static void runAsync(ApplicationEventPublisher publisher, String taskNameKey, Runnable task) {
        Locale locale = LocaleContextHolder.getLocale();
        String taskName = I18nUtils.message(taskNameKey, new Object[]{}, locale);
        publisher.publishEvent(new DataRefreshEvent(task, taskName, Status.STARTED,
                "data.refresh.started", new Object[]{taskName}, locale));
        ExecutorService executor = SpringUtils.getBean("taskExecutor");
        CompletableFuture.runAsync(() -> {
            try {
                task.run();
                publisher.publishEvent(new DataRefreshEvent(task, taskName, Status.COMPLETED,
                        "data.refresh.completed", new Object[]{taskName}, locale));
            } catch (Exception e) {
                publisher.publishEvent(new DataRefreshEvent(task, taskName, Status.FAILED,
                        "data.refresh.failed", new Object[]{taskName, e.getMessage()}, locale));
                throw e;
            }
        }, executor);
    }

    /**
     * 根据事件携带的 locale 解析国际化消息
     */
    public String resolveMessage() {
        try {
            return I18nUtils.message(messageKey, args, locale);
        } catch (Exception e) {
            return messageKey;
        }
    }

    public enum Status {
        STARTED, COMPLETED, FAILED
    }
}

package com.nyx.bot.utils;

import com.nyx.bot.enums.AsyncBeanName;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

@Slf4j
public class AsyncUtils {
    private static final AsyncUtils me = new AsyncUtils();
    /**
     * 异步操作任务调度线程池
     */
    private final Executor executor = SpringUtils.getBean("myAsync");

    /**
     * 单例模式
     */
    private AsyncUtils() {
    }

    public static AsyncUtils me() {
        return me;
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(Runnable task) {
        executor.execute(task);
    }

    /**
     * 执行任务
     * @param task 执行的方法
     * @param name 那个线程池
     */
    public void execute(Runnable task, AsyncBeanName name) {
        Executor executor = SpringUtils.getBean(name.getStr());
        executor.execute(task);
    }

}

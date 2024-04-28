package com.nyx.bot.config;


import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ShutdownHandler {


    private final UndertowServletWebServerFactory undertowServletWebServerFactory;

    private final List<Executor> threadPoolExecutors;


    @Autowired
    public ShutdownHandler(UndertowServletWebServerFactory undertowServletWebServerFactory, List<Executor> threadPoolExecutors) {
        this.undertowServletWebServerFactory = undertowServletWebServerFactory;
        this.threadPoolExecutors = threadPoolExecutors;
    }

    @PreDestroy
    public void shutdown() {
        undertowServletWebServerFactory.getWebServer().stop();
    }

    @PreDestroy
    public void shutdownAllExecutors() {
        for (Executor executor : threadPoolExecutors) {
            if (executor instanceof ThreadPoolExecutor threadPool) {
                threadPool.shutdown(); // 禁止提交新任务
                try {
                    // 等待一段时间，让现有任务终止
                    if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                        threadPool.shutdownNow(); // 取消当前正在执行的任务
                        // 请稍等片刻，让任务响应被取消
                        if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                            log.error("线程池未终止");
                        }
                    }
                } catch (InterruptedException ie) {
                    // （重新）如果当前线程也中断，则取消
                    threadPool.shutdownNow();
                    // 保留中断状态
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

}

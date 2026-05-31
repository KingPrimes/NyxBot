package com.nyx.bot.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Bean
    public ThreadFactory virtualThreadFactory() {
        return Thread.ofVirtual().name("virtual-", 0).factory();
    }

    @Bean(name = "myAsync")
    public ExecutorService myAsync() {
        log.info("start myAsync virtual thread executor");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "initDataExecutor")
    public ExecutorService initDataExecutor() {
        log.info("start initDataExecutor virtual thread executor");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        log.info("start scheduledExecutor");
        return Executors.newScheduledThreadPool(
                3,
                Thread.ofVirtual().name("scheduled-virtual-", 0).factory()
        );
    }

    @Bean("taskExecutor")
    public ExecutorService taskExecutor() {
        log.info("start taskExecutor virtual thread executor");
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}

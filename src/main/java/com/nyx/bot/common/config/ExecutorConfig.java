package com.nyx.bot.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize = 20;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize = 80;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity = 80;
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix = "async-";

    @Bean
    public ThreadFactory virtualThreadFactory() {
        // 虚拟线程命名格式: [前缀]-[序号]
        return Thread.ofVirtual().name("virtual-", 0).factory();
    }

    @Bean(name = "myAsync")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");
        return executor(namePrefix);
    }

    @Bean(name = "initDataExecutor")
    public ThreadPoolTaskExecutor initDataExecutor() {
        log.info("start initDataExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(1);
        //配置最大线程数
        executor.setMaxPoolSize(2);
        //配置队列大小
        executor.setQueueCapacity(20);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix("initData-");

        // 使用虚拟线程工厂（关键修改）
        executor.setThreadFactory(virtualThreadFactory());

        // 拒绝策略保持不变
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        //执行初始化
        executor.initialize();
        return executor;
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        log.info("start scheduledExecutor");
        return Executors.newScheduledThreadPool(
                3,  // 核心线程数（虚拟线程可适当提高）
                Thread.ofVirtual().name("scheduled-virtual-", 0).factory()
        );
    }

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        log.info("start taskExecutor");
        return executor("taskExecutor-");
    }


    private ThreadPoolTaskExecutor executor(String namePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数（虚拟线程可适当提高）
        executor.setCorePoolSize(corePoolSize);
        //配置最大线程数（虚拟线程数量可远大于CPU核心数）
        executor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        executor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);

        // 使用虚拟线程工厂（关键修改）
        executor.setThreadFactory(virtualThreadFactory());

        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(2);
        //执行初始化
        executor.initialize();
        return executor;
    }

}

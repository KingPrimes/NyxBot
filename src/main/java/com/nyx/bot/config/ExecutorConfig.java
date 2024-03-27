package com.nyx.bot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    @Value("${async.executor.thread.core_pool_size}")
    private int corePoolSize = 5;
    @Value("${async.executor.thread.max_pool_size}")
    private int maxPoolSize = 20;
    @Value("${async.executor.thread.queue_capacity}")
    private int queueCapacity = 80;
    @Value("${async.executor.thread.name.prefix}")
    private String namePrefix = "async-";

    @Bean(name = "myAsync")
    public ThreadPoolTaskExecutor asyncServiceExecutor() {
        log.info("start asyncServiceExecutor");
        return executor(namePrefix);
    }

    @Bean(name = "initDataExecutor")
    public ThreadPoolTaskExecutor initDataExecutor() {
        log.info("start initDataExecutor");
        return executor("initData-");
    }

    @Bean
    public ThreadPoolTaskExecutor scheduledExecutorService() {
        log.info("start scheduledExecutor");
        return executor("scheduled-");
    }

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        log.info("start taskExecutor");
        return executor("taskExecutor-");
    }


    private ThreadPoolTaskExecutor executor(String namePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //配置核心线程数
        executor.setCorePoolSize(corePoolSize);
        //配置最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        //配置队列大小
        executor.setQueueCapacity(queueCapacity);
        //配置线程池中的线程的名称前缀
        executor.setThreadNamePrefix(namePrefix);

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是有调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(2);
        //执行初始化
        executor.initialize();
        return executor;
    }
}

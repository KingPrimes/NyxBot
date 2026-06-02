package com.nyx.bot.common.config;

import jakarta.annotation.PreDestroy;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * 虚拟线程执行器配置
 * 使用 Semaphore 限制并发虚拟线程数，避免 2核服务器上过度争用平台线程
 */
@Slf4j
@Configuration
@EnableAsync
public class ExecutorConfig {

    /**
     * 虚拟线程最大并发数（2核建议 20-30）
     * Semaphore 限制防止无界虚拟线程创建导致平台线程饥饿
     */
    private static final int MAX_CONCURRENT_VIRTUAL_THREADS = 30;

    /**
     * 共享信号量，所有虚拟线程执行器共用此限制
     */
    private final Semaphore virtualThreadSemaphore = new Semaphore(MAX_CONCURRENT_VIRTUAL_THREADS);

    /**
     * 所有创建的 ExecutorService 实例，用于应用关闭时统一释放
     */
    private final List<ExecutorService> managedExecutors = new ArrayList<>();

    @Bean(name = "myAsync")
    public ExecutorService myAsync() {
        log.info("start myAsync virtual thread executor (max concurrent: {})", MAX_CONCURRENT_VIRTUAL_THREADS);
        return createBoundedVirtualThreadExecutor("myAsync");
    }

    @Bean(name = "initDataExecutor")
    public ExecutorService initDataExecutor() {
        log.info("start initDataExecutor virtual thread executor (max concurrent: {})", MAX_CONCURRENT_VIRTUAL_THREADS);
        return createBoundedVirtualThreadExecutor("initData");
    }

    /**
     * 定时任务调度器（2核服务器使用 2 个平台线程即可）
     */
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        log.info("start scheduledExecutor (pool: 2)");
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2,
                Thread.ofVirtual().name("scheduled-virtual-", 0).factory());
        managedExecutors.add(executor);
        return executor;
    }

    @Bean("taskExecutor")
    public ExecutorService taskExecutor() {
        log.info("start taskExecutor virtual thread executor (max concurrent: {})", MAX_CONCURRENT_VIRTUAL_THREADS);
        return createBoundedVirtualThreadExecutor("task");
    }

    /**
     * 应用关闭时优雅释放所有 ExecutorService
     * 先调用 shutdown() 等待任务完成，超时后强制 shutdownNow()
     */
    @PreDestroy
    public void destroy() {
        log.info("正在关闭所有 ExecutorService (共 {} 个)...", managedExecutors.size());
        for (ExecutorService executor : managedExecutors) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(8, TimeUnit.SECONDS)) {
                    log.warn("ExecutorService 未在 8s 内完成，强制关闭");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("所有 ExecutorService 已关闭");
    }

    /**
     * 创建有界虚拟线程执行器。
     * <p>
     * 设计要点：
     * 1. execute() — Semaphore protect 命名虚拟线程，许可在任务完成后释放
     * 2. submit() — 包装为 FutureTask 后走 execute()，确保经 Semaphore
     * 3. invokeAll/invokeAny — 包装每个 task 自带 Semaphore，通过 delegate 执行
     * 4. delegate — 仅用于 invokeAll/invokeAny 和 shutdown 语义，不用于 execute/submit
     *
     * @param name 线程名前缀，用于虚拟线程命名（线程转储时便于识别）
     * @return 有界 ExecutorService
     */
    private ExecutorService createBoundedVirtualThreadExecutor(String name) {
        ThreadFactory namedFactory = Thread.ofVirtual().name(name + "-", 0).factory();
        ExecutorService delegate = Executors.newVirtualThreadPerTaskExecutor();

        ExecutorService bounded = new ExecutorService() {
            @Override
            public void execute(@NonNull Runnable command) {
                virtualThreadSemaphore.acquireUninterruptibly();
                namedFactory.newThread(() -> {
                    try {
                        command.run();
                    } finally {
                        virtualThreadSemaphore.release();
                    }
                }).start();
            }

            @Override
            public void shutdown() {
                delegate.shutdown();
            }

            @NonNull
            @Override
            public List<Runnable> shutdownNow() {
                return delegate.shutdownNow();
            }

            @Override
            public boolean isShutdown() {
                return delegate.isShutdown();
            }

            @Override
            public boolean isTerminated() {
                return delegate.isTerminated();
            }

            @Override
            public boolean awaitTermination(long t, @NonNull TimeUnit u) throws InterruptedException {
                return delegate.awaitTermination(t, u);
            }

            // submit() 包装后走 execute()，确保经 Semaphore
            @NonNull
            @Override
            public <T> Future<T> submit(@NonNull Callable<T> task) {
                FutureTask<T> ft = new FutureTask<>(task);
                execute(ft);
                return ft;
            }

            @NonNull
            @Override
            public <T> Future<T> submit(@NonNull Runnable task, T result) {
                FutureTask<T> ft = new FutureTask<>(task, result);
                execute(ft);
                return ft;
            }

            @NonNull
            @Override
            public Future<?> submit(@NonNull Runnable task) {
                FutureTask<Void> ft = new FutureTask<>(task, null);
                execute(ft);
                return ft;
            }

            // invokeAll/invokeAny 包装每个 task 自带 Semaphore，通过 delegate 并行执行
            @NonNull
            @Override
            public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks)
                    throws InterruptedException {
                return delegate.invokeAll(tasks.stream().map(this::wrap).toList());
            }

            @NonNull
            @Override
            public <T> List<Future<T>> invokeAll(@NonNull Collection<? extends Callable<T>> tasks,
                                                 long t, @NonNull TimeUnit u) throws InterruptedException {
                return delegate.invokeAll(tasks.stream().map(this::wrap).toList(), t, u);
            }

            @NonNull
            @Override
            public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks)
                    throws InterruptedException, ExecutionException {
                return delegate.invokeAny(tasks.stream().map(this::wrap).toList());
            }

            @Override
            public <T> T invokeAny(@NonNull Collection<? extends Callable<T>> tasks,
                                   long t, @NonNull TimeUnit u)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return delegate.invokeAny(tasks.stream().map(this::wrap).toList(), t, u);
            }

            /**
             * 包装 Callable，执行前后获取/释放 Semaphore
             */
            private <T> Callable<T> wrap(Callable<T> task) {
                return () -> {
                    virtualThreadSemaphore.acquireUninterruptibly();
                    try {
                        return task.call();
                    } finally {
                        virtualThreadSemaphore.release();
                    }
                };
            }
        };

        // delegate 和 bounded 都注册到 managedExecutors，@PreDestroy 统一关闭
        managedExecutors.add(delegate);
        managedExecutors.add(bounded);
        return bounded;
    }
}

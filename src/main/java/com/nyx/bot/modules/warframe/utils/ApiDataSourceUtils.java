package com.nyx.bot.modules.warframe.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.utils.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component
public class ApiDataSourceUtils {

    private final ObjectMapper objectMapper;
    private final ExecutorService taskExecutor;

    public ApiDataSourceUtils(ObjectMapper objectMapper, ExecutorService taskExecutor) {
        this.objectMapper = objectMapper;
        this.taskExecutor = taskExecutor;
    }

    /**
     * 从多个数据源URL获取数据，返回第一个成功获取并解析的数据列表
     *
     * @param urls          数据源URL列表
     * @param typeReference 数据类型引用，用于反序列化响应数据
     * @param <T>           数据元素类型
     * @return 成功获取并解析的数据列表；如果所有数据源都失败则返回空列表
     */
    public <T> List<T> getDataFromSources(List<String> urls, TypeReference<List<T>> typeReference) {
        Map.Entry<String, List<T>> result = fetchWithRetries(urls, typeReference);
        if (result == null) {
            return List.of();
        }

        log.info("成功从 {} 获取数据", result.getKey());
        return result.getValue();
    }

    /**
     * 并发请求多个URL，返回第一个成功获取并解析数据的结果。
     * 使用虚拟线程执行器避免 pin 平台线程，失败的任务不会抢先取消尚未完成的请求。
     *
     * @param urls          URL列表
     * @param typeReference 数据类型引用，用于反序列化响应数据
     * @param <T>           数据元素类型
     * @return CompletableFuture，成功时包含 URL→数据 映射；所有URL都失败时返回 null
     */
    private <T> CompletableFuture<Map.Entry<String, List<T>>> firstCompleted(List<String> urls, TypeReference<List<T>> typeReference) {
        CompletableFuture<Map.Entry<String, List<T>>> result = new CompletableFuture<>();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String url : urls) {
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                try {
                    HttpUtils.Body body = HttpUtils.sendGet(url);
                    if (body.is2xxSuccessful()) {
                        List<T> dataList = objectMapper.readValue(body.body(), typeReference);
                        result.complete(Map.entry(url, dataList));
                    }
                } catch (Exception ignored) {
                    // 当前 URL 失败，其他 URL 可能成功，忽略
                }
            }, taskExecutor);
            futures.add(future);
        }

        // 所有请求都完成但无人成功时，返回 null
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenRun(() -> result.complete(null));

        // 有人成功或全部失败时，取消尚未完成的请求
        return result.whenComplete((r, t) -> {
            for (CompletableFuture<?> f : futures) {
                if (!f.isDone()) {
                    f.cancel(true);
                }
            }
        });
    }

    /**
     * 带重试机制的数据获取方法
     *
     * @param <T>           数据元素类型
     * @param urls          URL列表
     * @param typeReference 数据类型引用，用于反序列化响应数据
     * @return Map.Entry，键为成功获取数据的URL，值为解析后的数据列表；如果所有重试都失败则返回null
     */
    private <T> Map.Entry<String, List<T>> fetchWithRetries(List<String> urls, TypeReference<List<T>> typeReference) {
        int retryCount = 0;
        int maxRetries = 2;
        // 循环重试直到达到最大重试次数
        while (retryCount <= maxRetries) {
            if (retryCount > 0) {
                log.info("获取数据失败，正在进行第{}次重试", retryCount);
            }

            try {
                // 调用firstCompleted方法获取第一个成功的结果
                Map.Entry<String, List<T>> result = firstCompleted(urls, typeReference).get();
                if (result != null) {
                    return result;
                }
                log.warn("所有数据源都未能成功获取数据，即将进行重试获取。");
            } catch (Exception e) {
                log.error("获取数据时发生异常: {}", e.getMessage());
            }

            // 添加2秒延迟再进行重试
            if (retryCount < maxRetries) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {

                }
            }

            retryCount++;
        }
        return null;
    }

}

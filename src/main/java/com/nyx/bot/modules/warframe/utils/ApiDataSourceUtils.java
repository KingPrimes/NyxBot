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

@Slf4j
@Component
public class ApiDataSourceUtils {

    private final ObjectMapper objectMapper;

    public ApiDataSourceUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
     * 并发请求多个URL，返回第一个成功获取并解析数据的结果
     *
     * @param urls          URL列表
     * @param typeReference 数据类型引用，用于反序列化响应数据
     * @param <T>           数据元素类型
     * @return CompletableFuture包装的Map.Entry，键为成功获取数据的URL，值为解析后的数据列表；
     * 如果所有URL都失败则返回null
     */
    private <T> CompletableFuture<Map.Entry<String, List<T>>> firstCompleted(List<String> urls, TypeReference<List<T>> typeReference) {
        // 为每个URL创建异步HTTP请求任务
        List<CompletableFuture<Map.Entry<String, List<T>>>> futures = new ArrayList<>();
        for (String url : urls) {
            futures.add(
                    CompletableFuture
                            .supplyAsync(() -> {
                                HttpUtils.Body body = HttpUtils.sendGet(url);
                                if (body.code().is2xxSuccessful()) {
                                    try {
                                        List<T> dataList = objectMapper.readValue(body.body(), typeReference);
                                        return Map.entry(url, dataList);
                                    } catch (Exception e) {
                                        // 解析失败，返回null
                                        return null;
                                    }
                                }
                                // HTTP状态码不是2xx，返回null
                                return null;
                            })
            );
        }
        // 等待任一项任务完成
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));
        // 处理结果并取消未完成的任务
        return anyOf
                .thenApply(obj -> (Map.Entry<String, List<T>>) obj)
                .whenComplete((r, t) -> cancelUnfinishedFutures(futures));
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

    /**
     * 取消所有未完成的请求
     *
     * @param futures 请求列表
     */
    private <T> void cancelUnfinishedFutures(List<CompletableFuture<Map.Entry<String, List<T>>>> futures) {
        for (CompletableFuture<Map.Entry<String, List<T>>> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }
}

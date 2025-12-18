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

    @SuppressWarnings("unchecked")
    public <T> List<T> getDataFromSources(List<String> urls, TypeReference<List<T>> typeReference) {
        List<T> dataList = new ArrayList<>();
        int retryCount = 0;
        final int maxRetries = 2;

        while (retryCount <= maxRetries) {
            if (retryCount > 0) {
                log.info("获取数据失败，正在进行第{}次重试", retryCount);
            }

            List<CompletableFuture<Map.Entry<String, HttpUtils.Body>>> futures = new ArrayList<>();

            // 创建并发请求，将URL与响应关联
            for (String url : urls) {
                CompletableFuture<Map.Entry<String, HttpUtils.Body>> future = CompletableFuture
                        .supplyAsync(() -> Map.entry(url, HttpUtils.sendGet(url)));
                futures.add(future);
            }

            // 等待任意一个请求成功完成
            CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(futures.toArray(new CompletableFuture[0]));

            try {
                Map.Entry<String, HttpUtils.Body> result = (Map.Entry<String, HttpUtils.Body>) anyOfFuture.get();
                String successUrl = result.getKey();
                HttpUtils.Body body = result.getValue();

                if (body.code().is2xxSuccessful()) {
                    try {
                        dataList = objectMapper.readValue(body.body(), typeReference);
                        log.info("成功从 {} 获取数据", successUrl);
                        cancelUnfinishedFutures(futures);
                        break;
                    } catch (Exception e) {
                        log.warn("从 {} 解析数据失败: {}", successUrl, e.getMessage());
                    }
                } else {
                    log.warn("从 {} 获取数据失败: HttpCode {}", successUrl, body.code());
                }
            } catch (Exception e) {
                log.error("获取数据时发生异常: {}", e.getMessage());
            }

            // 如果执行到这里说明本次尝试失败，增加重试计数
            retryCount++;
        }

        return dataList;
    }

    /**
     * 取消所有未完成的请求
     *
     * @param futures 请求列表
     */
    private void cancelUnfinishedFutures(List<CompletableFuture<Map.Entry<String, HttpUtils.Body>>> futures) {
        for (CompletableFuture<Map.Entry<String, HttpUtils.Body>> future : futures) {
            if (!future.isDone()) {
                future.cancel(true);
            }
        }
    }
}

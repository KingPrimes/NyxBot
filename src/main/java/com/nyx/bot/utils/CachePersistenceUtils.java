package com.nyx.bot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CachePersistenceUtils {

    private CachePersistenceUtils() {
    }

    public static void setAndPersist(String cacheName, Object value, long duration, TimeUnit unit,
                                      String filePath, ObjectMapper objectMapper, String logName) {
        CacheUtils.set(cacheName, "data", value, duration, unit);
        try {
            FileUtils.writeFile(filePath, objectMapper.writeValueAsBytes(value));
        } catch (Exception e) {
            log.error("序列化{}失败: {}", logName, e.getMessage());
        }
    }
}

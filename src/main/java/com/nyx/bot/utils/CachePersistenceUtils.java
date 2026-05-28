package com.nyx.bot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CachePersistenceUtils {

    private CachePersistenceUtils() {
    }

    /**
     * 将缓存数据同时写入缓存和持久化到本地文件（JSON字节格式）
     * <p>内部使用 {@link ObjectMapper#writeValueAsBytes(Object)} 将对象序列化为JSON字节后写入文件</p>
     *
     * @param cacheName 缓存名称，对应 Cache2k 的缓存区域标识
     * @param value     要缓存的值对象，将被序列化为JSON写入文件
     * @param duration  缓存过期时长
     * @param unit      时长单位（如 TimeUnit.MINUTES）
     * @param filePath  持久化文件的本地路径
     * @param mapper    Jackson ObjectMapper 实例，用于JSON序列化
     * @param logName   日志中标识该数据的名称，用于失败时的错误信息
     */
    public static void setAndPersist(String cacheName, Object value, long duration, TimeUnit unit,
                                      String filePath, ObjectMapper mapper, String logName) {
        try {
            doPersist(cacheName, value, duration, unit, filePath, mapper.writeValueAsBytes(value), logName);
        } catch (Exception e) {
            log.error("序列化{}失败: {}", logName, e.getMessage());
        }
    }

    /**
     * 将缓存数据同时写入缓存和持久化到本地文件（自定义字节格式）
     * <p>适用于需要自定义序列化逻辑的场景，如Base64编码等</p>
     *
     * @param cacheName 缓存名称，对应 Cache2k 的缓存区域标识
     * @param value     要缓存的值对象，存入缓存供运行时读取
     * @param duration  缓存过期时长
     * @param unit      时长单位（如 TimeUnit.DAYS）
     * @param filePath  持久化文件的本地路径
     * @param bytes     已序列化好的字节数组，将直接写入文件
     * @param logName   日志中标识该数据的名称，用于失败时的错误信息
     */
    public static void setAndPersist(String cacheName, Object value, long duration, TimeUnit unit,
                                      String filePath, byte[] bytes, String logName) {
        doPersist(cacheName, value, duration, unit, filePath, bytes, logName);
    }

    /**
     * 将缓存数据同时写入缓存和持久化到本地文件（字符串格式）
     * <p>将字符串以UTF-8编码转换为字节后写入文件，适用于Base64编码字符串等场景</p>
     *
     * @param cacheName 缓存名称，对应 Cache2k 的缓存区域标识
     * @param value     要缓存的值对象，存入缓存供运行时读取
     * @param duration  缓存过期时长
     * @param unit      时长单位
     * @param filePath  持久化文件的本地路径
     * @param str       已序列化为字符串的数据，将以UTF-8编码写入文件
     * @param logName   日志中标识该数据的名称，用于失败时的错误信息
     */
    public static void setAndPersist(String cacheName, Object value, long duration, TimeUnit unit,
                                      String filePath, String str, String logName) {
        doPersist(cacheName, value, duration, unit, filePath, str.getBytes(StandardCharsets.UTF_8), logName);
    }

    /**
     * 执行实际的缓存写入和文件持久化操作
     *
     * @param cacheName 缓存名称
     * @param value     缓存值对象
     * @param duration  过期时长
     * @param unit      时长单位
     * @param filePath  本地文件路径
     * @param bytes     写入文件的字节数据
     * @param logName   日志数据名称标识
     */
    private static void doPersist(String cacheName, Object value, long duration, TimeUnit unit,
                                   String filePath, byte[] bytes, String logName) {
        CacheUtils.set(cacheName, "data", value, duration, unit);
        try {
            FileUtils.writeFile(filePath, bytes);
        } catch (Exception e) {
            log.error("序列化{}失败: {}", logName, e.getMessage());
        }
    }
}

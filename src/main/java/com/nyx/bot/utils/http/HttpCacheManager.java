package com.nyx.bot.utils.http;

import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.http.HttpUtils.Body;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 响应缓存管理
 * 基于 Cache2k 的缓存读写，用于减少重复 API 请求
 *
 * @author KingPrimes
 */
@Slf4j
class HttpCacheManager {

    /**
     * HTTP 响应缓存名称，对应 Cache2k 缓存区域
     */
    static final String HTTP_RESPONSE_CACHE = "http-response";

    /**
     * 生成缓存 Key（URL + param 的 SHA-256 前16位）
     */
    static String buildCacheKey(String url, String param) {
        String raw = url + (param != null && !param.isEmpty() ? "?" + param : "");
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8 && i < hash.length; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(raw.hashCode());
        }
    }

    /**
     * 从缓存读取 HTTP 响应
     *
     * @return 缓存的 Body，未命中、非2xx或缓存不可用时返回 null
     */
    static Body cacheGet(String key) {
        try {
            Body cached = CacheUtils.get(HTTP_RESPONSE_CACHE, key, Body.class);
            if (cached != null && cached.code() != null && cached.code().is2xxSuccessful()) {
                log.debug("HTTP缓存命中: key={}", key);
                return cached;
            }
        } catch (Throwable e) {
            log.debug("HTTP缓存读取失败(缓存不可用): {}", e.getMessage());
        }
        return null;
    }

    /**
     * 将 HTTP 响应写入缓存
     */
    static void cachePut(String key, Body body, long cacheSeconds) {
        try {
            CacheUtils.putWithExpiry(HTTP_RESPONSE_CACHE, key, body, cacheSeconds, TimeUnit.SECONDS);
            log.debug("HTTP缓存写入: key={}, ttl={}s", key, cacheSeconds);
        } catch (Throwable e) {
            log.debug("HTTP缓存写入失败(缓存不可用): {}", e.getMessage());
        }
    }
}

package com.nyx.bot.utils;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.extra.spring.SpringCache2kCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CacheUtils {
    public static final String SYSTEM = "system";
    public static final String WARFRAME_STATUS = "warframe-status";
    public static final String WARFRAME_GLOBAL_STATES = "global-states";

    public static final String WARFRAME = "warframe";

    public static final String WARFRAME_GLOBAL_STATES_ARBITRATION = "global-states-arbitration";

    private static final CacheManager cm = SpringUtils.getBean(CacheManager.class);


    /**
     * 设置缓存
     *
     * @param name 缓存名称
     * @param map  map k,v
     */
    public static void set(String name, Map<Object, Object> map) {
        map.forEach((k, v) -> Objects.requireNonNull(cm.getCache(name)).put(k, v));
    }

    /**
     * 设置缓存
     *
     * @param name 缓存名称
     * @param kv   KeyAndValue key,value
     */
    public static void set(String name, Object... kv) {
        //动态参数缺少数据
        if (kv.length % 2 != 0) {
            log.error("键值对缺少！");
            return;
        }
        //遍历动态参数
        for (int i = 0; i < kv.length + 1; ) {
            if (i + 1 < kv.length + 1) {
                Objects.requireNonNull(cm.getCache(name)).put(kv[i], kv[i + 1]);
            }
            if (i + 2 < kv.length + 1) {
                i += 2;
            } else {
                i++;
            }
        }
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @param key  key
     * @return Object
     */
    public static Object get(String name, Object key) {
        return cm.getCache(name).get(key).get();
    }

    /**
     * 获取缓存
     *
     * @param name 缓存名称
     * @param key  key
     * @param type 缓存的类
     * @return type参数的类
     */
    public static <T> T get(String name, Object key, @NotNull Class<T> type) {
        return cm.getCache(name).get(key, type);
    }

    public static boolean exists(String name, Object key) {
        return cm.getCache(name).get(key) != null;
    }

    /**
     * 存入缓存并指定过期时间（动态设置）
     *
     * @param cacheName 缓存名称
     * @param key       键
     * @param value     值
     * @param duration  时间长度
     * @param unit      时间单位
     */
    public static void putWithExpiry(String cacheName, Object key, Object value, long duration, TimeUnit unit) {
        try {
            Cache springCache = cm.getCache(cacheName);
            if (springCache instanceof SpringCache2kCache) {
                // 获取 Cache2k 原生缓存实例
                org.cache2k.Cache<Object, Object> nativeCache = ((SpringCache2kCache) springCache).getNativeCache();
                long expiryTime = System.currentTimeMillis() + unit.toMillis(duration);
                // 通过 EntryProcessor 设置值和过期时间
                nativeCache.invoke(key, entry -> {
                    entry.setValue(value);
                    entry.setExpiryTime(expiryTime);
                    return entry;
                });
            } else {
                // 非 Cache2k 缓存实现时的回退逻辑
                springCache.put(key, value);
            }
        } catch (IllegalStateException e) {
            log.warn("CacheManager已关闭，无法设置带过期时间的缓存: {}", cacheName);
        }
    }
}

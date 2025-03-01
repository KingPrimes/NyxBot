package com.nyx.bot.config;

import com.nyx.bot.utils.CacheUtils;
import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CachingConfig {

    /***
     * 配置Cache缓存管理器 交给Spring Boot管理
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        SpringCache2kCacheManager cacheManager = new SpringCache2kCacheManager()
                .addCaches(
                        //配置名称为 warframe-socket-data 的缓存策略， eternal永不过期， entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_SOCKET_DATA).eternal(true).entryCapacity(1),
                        //配置名称为 system 的缓存策略，permitNullValues允许为空值，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.SYSTEM).permitNullValues(false).entryCapacity(100).expireAfterWrite(10, TimeUnit.MINUTES),
                        b -> b.name(CacheUtils.WARFRAME),
                        // 配置名称为 global-states 的缓存策略，expireAfterWrite设置过期时间，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_GLOBAL_STATES).entryCapacity(1).expireAfterWrite(30, TimeUnit.MINUTES),
                        // 配置名称为 global-states-arbitration 的缓存策略，expireAfterWrite设置过期时间，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_GLOBAL_STATES_ARBITRATION).entryCapacity(1).expireAfterWrite(1, TimeUnit.DAYS)
                );
        cacheManager.setAllowUnknownCache(true);
        //cacheManager.defaultSetup(builder -> builder.entryCapacity(100).disableStatistics(true));
        return cacheManager;

    }

}

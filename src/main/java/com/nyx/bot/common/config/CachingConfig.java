package com.nyx.bot.common.config;

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
                        //配置名称为 system 的缓存策略，permitNullValues允许为空值，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.SYSTEM).permitNullValues(false).entryCapacity(100).expireAfterWrite(10, TimeUnit.MINUTES),
                        b -> b.name(CacheUtils.WARFRAME).expireAfterWrite(30, TimeUnit.MINUTES).entryCapacity(2),
                        // 配置名称为 global-states 的缓存策略，expireAfterWrite设置过期时间，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_GLOBAL_STATES).entryCapacity(1).expireAfterWrite(30, TimeUnit.MINUTES),
                        // 配置名称为 global-states-arbitration 的缓存策略，expireAfterWrite设置过期时间，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_GLOBAL_STATES_ARBITRATION).entryCapacity(1).expireAfterWrite(1, TimeUnit.DAYS),
                        // 令牌黑名单，jti → true，过期时间由每次put时设置为token剩余有效期
                        b -> b.name(CacheUtils.TOKEN_BLACKLIST).permitNullValues(false).entryCapacity(1000)
                );
        cacheManager.setAllowUnknownCache(true);
        //cacheManager.defaultSetup(builder -> builder.entryCapacity(100).disableStatistics(true));
        return cacheManager;

    }

}

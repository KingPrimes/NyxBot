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
        return new SpringCache2kCacheManager()
                .addCaches(
                        //配置名称为 warframe-socket-data 的缓存策略， eternal永不过期， entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.WARFRAME_SOCKET_DATA).eternal(true).entryCapacity(1),
                        //配置名称为 group-captcha 的缓存策略，expireAfterWrite设置过期时间，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.GROUP_CAPTCHA).expireAfterWrite(30, TimeUnit.SECONDS).entryCapacity(10000),
                        //配置名称为 system 的缓存策略，permitNullValues允许为空值，entryCapacity 可以有多少个缓存
                        b -> b.name(CacheUtils.SYSTEM).permitNullValues(false).entryCapacity(100).expireAfterWrite(10,TimeUnit.MINUTES));
    }

}

package com.nyx.bot.cache;

import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import io.github.kingprimes.model.WorldState;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_STATUS;

@Slf4j
public class WarframeCache {

    /**
     * 从缓存中获取当前的Warframe世界状态数据
     *
     * @return 世界状态对象，包含警报、入侵、突击等信息
     * @throws DataNotInfoException 缓存中无数据时抛出
     */
    public static WorldState getWarframeStatus() throws DataNotInfoException {
        WorldState worldState = CacheUtils.get(WARFRAME_STATUS, "data", WorldState.class);
        if (worldState == null) {
            throw new DataNotInfoException(I18nUtils.message("error.warframe.data.null"));
        }
        return worldState;
    }

    /**
     * 设置Warframe世界状态数据并持久化到本地文件
     * <p>直接保存原始JSON（避免反序列化再序列化导致的体积膨胀，实测可减少 95%+），
     * 再写入缓存（带 TTL）</p>
     *
     * @param object     世界状态数据对象（用于缓存查询）
     * @param rawJson    API 原始 JSON 字符串（用于文件持久化）
     * @param ttlSeconds 缓存过期时间（秒），与下次轮询时间对齐
     */
    public static void setWarframeStatus(Object object, String rawJson, long ttlSeconds) {
        try {
            FileUtils.writeFile("./data/status", rawJson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("持久化WorldState失败: {}", e.getMessage());
            return;
        }
        CacheUtils.putWithExpiry(WARFRAME_STATUS, "data", object, ttlSeconds, TimeUnit.SECONDS);
    }
}

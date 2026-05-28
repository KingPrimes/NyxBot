package com.nyx.bot.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.utils.CachePersistenceUtils;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.model.WorldState;
import lombok.extern.slf4j.Slf4j;

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
     * <p>同时将数据写入缓存(3分钟TTL)和本地JSON文件用于重启后恢复</p>
     *
     * @param object 世界状态数据对象
     */
    public static void setWarframeStatus(Object object) {
        ObjectMapper mapper = SpringUtils.getBean(ObjectMapper.class);
        CachePersistenceUtils.setAndPersist(
                WARFRAME_STATUS, object, 3L, TimeUnit.MINUTES,
                "./data/status", mapper, "WorldState");
    }
}

package com.nyx.bot.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import io.github.kingprimes.model.WorldState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_STATUS;

@Slf4j
public class WarframeCache {

    private static final ObjectMapper objectMapper = SpringUtils.getBean(ObjectMapper.class);

    public static WorldState getWarframeStatus() throws DataNotInfoException {
        WorldState worldState = CacheUtils.get(WARFRAME_STATUS, "data", WorldState.class);
        if (worldState == null) {
            throw new DataNotInfoException(I18nUtils.message("error.warframe.data.null"));
        }
        return worldState;
    }

    public static void setWarframeStatus(Object object) {
        CacheUtils.set(WARFRAME_STATUS, "data", object, 3L, TimeUnit.MINUTES);
        try {
            FileUtils.writeFile("./data/status", objectMapper.writeValueAsBytes(object));
        } catch (Exception e) {
            log.error("序列化WorldState失败: {}", e.getMessage());
        }
    }
}

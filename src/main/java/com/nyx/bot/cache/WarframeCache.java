package com.nyx.bot.cache;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.exception.DataNotInfoException;

import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.FileUtils;
import com.nyx.bot.utils.I18nUtils;
import io.github.kingprimes.model.WorldState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.nyx.bot.utils.CacheUtils.WARFRAME_STATUS;

@Slf4j
public class WarframeCache {
    public static WorldState getWarframeStatus() throws DataNotInfoException {
        WorldState worldState = CacheUtils.get(WARFRAME_STATUS, "data", WorldState.class);
        if (worldState == null) {
            throw new DataNotInfoException(I18nUtils.message("error.warframe.data.null"));
        }
        return worldState;
    }

    public static void setWarframeStatus(Object object) {
        CacheUtils.set(WARFRAME_STATUS, "data", object, 3L, TimeUnit.MINUTES);
        FileUtils.writeFile("./data/status", JSON.toJSONBytes(object));
    }
}

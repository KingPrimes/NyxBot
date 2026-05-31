package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.utils.MarketDucatsUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.Ducats;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public abstract class AbstractMarketDumpPlugin {

    protected final DrawImagePlugin drawImagePlugin;
    protected final MarketDucatsUtils marketDucatsUtils;

    protected AbstractMarketDumpPlugin(DrawImagePlugin drawImagePlugin, MarketDucatsUtils marketDucatsUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.marketDucatsUtils = marketDucatsUtils;
    }

    protected void handle(Bot bot, AnyMessageEvent event) {
        Ducats ducats = marketDucatsUtils.getDucats();
        if (Objects.isNull(ducats)) {
            log.debug("获取ducat失败");
            bot.sendMsg(event, "获取ducat失败", false);
            return;
        }

        byte[] bytes = drawImagePlugin.drawMarketGodDumpImage(
                marketDucatsUtils.getDuats(getDucatsType(), ducats));
        SendUtils.send(bot, event, bytes, getCode(), log);
    }

    protected abstract MarketDucatsUtils.DucatsType getDucatsType();

    protected abstract Codes getCode();
}

package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.MarketDucatsUtils;
import io.github.kingprimes.DrawImagePlugin;
import org.springframework.stereotype.Component;

/**
 * 查询 Market市场中的银垃圾
 */
@Shiro
@Component
public class MarketSilverDumpPlugin extends AbstractMarketDumpPlugin {

    public MarketSilverDumpPlugin(DrawImagePlugin drawImagePlugin, MarketDucatsUtils marketDucatsUtils) {
        super(drawImagePlugin, marketDucatsUtils);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_SILVER_DUMP_CMD, at = AtEnum.BOTH)
    public void marketSilverDumpHandler(Bot bot, AnyMessageEvent event) {
        handle(bot, event);
    }

    @Override
    protected MarketDucatsUtils.DucatsType getDucatsType() {
        return MarketDucatsUtils.DucatsType.SILVER;
    }

    @Override
    protected Codes getCode() {
        return Codes.WARFRAME_MARKET_SILVER_DUMP;
    }
}

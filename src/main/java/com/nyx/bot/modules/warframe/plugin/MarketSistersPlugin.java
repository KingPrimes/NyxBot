package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.MarketLichSisterUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.market.MarketLichSister;
import org.springframework.stereotype.Component;

/**
 * 查询 Market Sisters 市场拍卖
 */
@Shiro
@Component
public class MarketSistersPlugin extends AbstractMarketLichSisterPlugin {

    public MarketSistersPlugin(DrawImagePlugin drawImagePlugin, MarketLichSisterUtils marketLichSisterUtils) {
        super(drawImagePlugin, marketLichSisterUtils);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SISTERS_CMD, at = AtEnum.BOTH)
    public void marketSisters(Bot bot, AnyMessageEvent event) {
        handle(bot, event, CommandConstants.WARFRAME_SISTERS_CMD);
    }

    @Override
    protected MarketLichSisterUtils.SearchType getSearchType() {
        return MarketLichSisterUtils.SearchType.SISTER;
    }

    @Override
    protected byte[] getResultImage(MarketLichSister result) {
        return drawImagePlugin.drawMarketSisterImage(result);
    }

    @Override
    protected Codes getCode() {
        return Codes.WARFRAME_SISTERS_PLUGIN;
    }
}

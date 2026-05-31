package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.utils.MarketLichSisterUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.market.MarketLichSister;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMarketLichSisterPlugin {

    protected final DrawImagePlugin drawImagePlugin;
    protected final MarketLichSisterUtils marketLichSisterUtils;

    protected AbstractMarketLichSisterPlugin(DrawImagePlugin drawImagePlugin, MarketLichSisterUtils marketLichSisterUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.marketLichSisterUtils = marketLichSisterUtils;
    }

    protected void handle(Bot bot, AnyMessageEvent event, String commandPrefix) {
        String key = event.getMessage().replaceAll(commandPrefix, "").trim();
        if (key.isEmpty()) {
            log.debug("用户:{} 输入了错误的指令 {}", event.getUserId(), event.getMessage());
            bot.sendMsg(event, "请输入正确的指令！", false);
            return;
        }
        MarketResult<LichSisterWeapons, MarketLichSister> auctions =
                marketLichSisterUtils.getAuctions(key, getSearchType());
        if (auctions.getPossibleItems() != null) {
            byte[] bytes = drawImagePlugin.drawMarketOrdersImage(auctions.getPossibleItems());
            SendUtils.send(bot, event, bytes, getCode(), log);
            return;
        }
        byte[] bytes = getResultImage(auctions.getResult());
        SendUtils.send(bot, event, bytes, getCode(), log);
    }

    protected abstract MarketLichSisterUtils.SearchType getSearchType();

    protected abstract byte[] getResultImage(MarketLichSister result);

    protected abstract Codes getCode();
}

package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.MarketRivenUtils;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询 Market 市场拍卖的紫卡
 */
@Shiro
@Component
@Slf4j
public class MarketRivenPlugin {
    private final DrawImagePlugin drawImagePlugin;

    private final MarketRivenUtils marketRivenUtils;

    public MarketRivenPlugin(MarketRivenUtils marketRivenUtils, DrawImagePlugin drawImagePlugin) {
        this.marketRivenUtils = marketRivenUtils;
        this.drawImagePlugin = drawImagePlugin;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_RIVEN_CMD, at = AtEnum.BOTH)
    public void marketRiven(Bot bot, AnyMessageEvent event) {
        String str = event.getMessage();
        if (MatcherUtils.isSpecialSymbols(str)) {
            String item = MatcherUtils.isOrderItem(str);
            if (item.isEmpty()) {
                log.debug("用户:{} 输入了错误的指令 {}", event.getUserId(), str);
                bot.sendMsg(event, "请输入正确的指令！", false);
                return;
            }
            str = item;
        }
        String key = str.replaceAll(Codes.WARFRAME_MARKET_RIVEN_PLUGIN.getComm(), "").trim();
        if (key.isEmpty()) {
            log.debug("用户:{} 输入了错误的指令 {}", event.getUserId(), str);
            bot.sendMsg(event, "请输入正确的指令！", false);
            return;
        }
        byte[] bytes = postMarketRivenImage(key);
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_MARKET_RIVEN_PLUGIN, log);
    }

    private byte[] postMarketRivenImage(String key) {
        return drawImagePlugin.drawMarketRivenImage(marketRivenUtils.marketRivenParameter(key));
    }
}

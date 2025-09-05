package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 查询 Market 市场拍卖的紫卡
 */
@Shiro
@Component
@Slf4j
public class MarketRivenPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_RIVEN_CMD)
    public void marketRiven(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.WARFRAME_MARKET_RIVEN_CMD);
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

    private byte[] postMarketRivenImage(String key) throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/marketRiven", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("riven", MarketUtils.marketRivenParameter(key));
            return modelMap;
        }).toByteArray();
    }
}

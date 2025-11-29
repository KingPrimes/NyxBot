package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.entity.LichSisterWeapons;
import com.nyx.bot.modules.warframe.entity.MarketResult;
import com.nyx.bot.modules.warframe.utils.MarketLichsSisterUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.market.MarketLichSister;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询 Market Sisters 市场拍卖
 */
@Shiro
@Component
@Slf4j
public class MarketSistersPlugin {


    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SISTERS_CMD, at = AtEnum.BOTH)
    public void marketSisters(Bot bot, AnyMessageEvent event) {
        String key = event.getMessage().replaceAll(CommandConstants.WARFRAME_SISTERS_CMD, "").trim();
        if (key.isEmpty()) {
            log.debug("用户:{} 输入了错误的指令 {}", event.getUserId(), event.getMessage());
            bot.sendMsg(event, "请输入正确的指令！", false);
            return;
        }
        MarketResult<LichSisterWeapons, MarketLichSister> auctions = MarketLichsSisterUtils.getAuctions(key, MarketLichsSisterUtils.SearchType.SISTER);
        if (auctions.getPossibleItems() != null) {
            byte[] bytes = drawImagePlugin.drawMarketOrdersImage(auctions.getPossibleItems());
            SendUtils.send(bot, event, bytes, Codes.WARFRAME_SISTERS_PLUGIN, log);
            return;
        }
        byte[] bytes = drawImagePlugin.drawMarketSisterImage(auctions.getResult());
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_SISTERS_PLUGIN, log);
    }
}

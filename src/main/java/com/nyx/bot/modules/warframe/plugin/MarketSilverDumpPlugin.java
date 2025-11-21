package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.DucatsUtils;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.Ducats;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 查询 Market市场中的银垃圾
 */
@Shiro
@Component
@Slf4j
public class MarketSilverDumpPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_SILVER_DUMP_CMD, at = AtEnum.BOTH)
    public void marketSilverDumpHandler(Bot bot, AnyMessageEvent event) {
        Ducats ducats = MarketUtils.getDucats();
        if (Objects.isNull(ducats)) {
            log.debug("获取ducat失败");
            bot.sendMsg(event, "获取ducat失败", false);
            return;
        }

        byte[] bytes = drawImagePlugin.drawMarketGodDumpImage(DucatsUtils.getDuats(DucatsUtils.DucatsType.SILVER, ducats));
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_MARKET_SILVER_DUMP, log);
    }
}

package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 每日特惠
 */
@Shiro
@Component
@Slf4j
public class DailyDealsPlugin {
    private final DrawImagePlugin drawImagePlugin;

    private final WorldStateUtils worldStateUtils;


    public DailyDealsPlugin(DrawImagePlugin drawImagePlugin, WorldStateUtils worldStateUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.worldStateUtils = worldStateUtils;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_DAILY_DEALS_CMD, at = AtEnum.BOTH)
    public void dailyDealsHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postDailyDealsImage(), Codes.WARFRAME_DAILY_DEALS_PLUGIN, log);
    }

    private byte[] postDailyDealsImage() throws DataNotInfoException {
        return drawImagePlugin.drawDailyDealsImage(worldStateUtils.getDailyDeals().getFirst());
    }
}

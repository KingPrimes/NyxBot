package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 警报
 */
@Shiro
@Component
@Slf4j
public class AlertsPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALERTS_CMD,at = AtEnum.BOTH)
    public void alertsHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, HtmlToImage.generateImageBytes("html/alerts", WarframeCache.getWarframeStatus().getAlerts(), "alerts"), Codes.WARFRAME_ALERTS_PLUGIN, log);
    }
}

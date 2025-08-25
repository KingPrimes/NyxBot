package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
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
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALERTS_CMD)
    public void alertsHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "getAlertsImage", Codes.WARFRAME_ALERTS_PLUGIN, log);
    }
}

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
 * 执刑官猎杀
 */
@Shiro
@Component
@Slf4j
public class LiteSoritePlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_LITE_SORITE_CMD)
    public void liteSoriteHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postArsonHuntImage", Codes.WARFRAME_LITE_SORITE_PLUGIN, log);
    }
}

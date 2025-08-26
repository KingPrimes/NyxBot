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
 * 突击
 */
@Shiro
@Component
@Slf4j
public class SortiesPlugin {

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SORTIES_CMD)
    public void sortiesHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postAssaultImage", Codes.WARFRAME_SORTIES_PLUGIN, log);
    }
}

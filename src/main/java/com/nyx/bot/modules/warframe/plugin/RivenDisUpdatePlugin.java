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
 * 紫卡倾向变动
 */
@Shiro
@Component
@Slf4j
public class RivenDisUpdatePlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_DIS_UPDATE_CMD)
    public void rivenDisUpdate(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postRivenDispositionUpdatesImage", Codes.WARFRAME_RIVEN_DIS_UPDATE_PLUGIN, log);
    }
}

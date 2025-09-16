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
 * 奸商
 */
@Shiro
@Component
@Slf4j
public class VoidPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_CMD)
    public void voidHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postVoidImage", Codes.WARFRAME_VOID_PLUGIN, log);
    }
}

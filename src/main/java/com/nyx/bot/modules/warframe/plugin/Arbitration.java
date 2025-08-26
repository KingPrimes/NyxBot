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
 * 仲裁
 */
@Shiro
@Component
@Slf4j
public class Arbitration {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_CMD)
    public void arbitrationHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postArbitrationImage", Codes.WARFRAME_ARBITRATION_PLUGIN, log);
    }
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_EX_CMD)
    public void arbitrationExHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postArbitrationExImage", Codes.WARFRAME_ARBITRATION_EX_PLUGIN, log);
    }
}

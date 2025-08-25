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
 * 入侵
 */
@Shiro
@Component
@Slf4j
public class InvasionPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_INVASIONS_CMD)
    public void invasionHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postInvasionsImage", Codes.WARFRAME_INVASIONS_PLUGIN, log);
    }
}

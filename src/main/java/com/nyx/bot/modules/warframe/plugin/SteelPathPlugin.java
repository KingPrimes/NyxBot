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
 * 钢铁奖励轮换
 */
@Shiro
@Component
@Slf4j
public class SteelPathPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_STEEL_PATH_CMD)
    public void steelPathHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postSteelPathImage", Codes.WARFRAME_STEEL_PATH_PLUGIN, log);
    }
}

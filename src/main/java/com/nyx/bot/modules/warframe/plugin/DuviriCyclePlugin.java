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
 * 双衍王境 轮换
 */
@Shiro
@Component
@Slf4j
public class DuviriCyclePlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_DUVIRI_CYCLE_CMD)
    public void duviriCycle(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postDuviriCycleImage", Codes.WARFRAME_DUVIRI_CYCLE, log);
    }
}

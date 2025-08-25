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
 * 平原时间
 */
@Shiro
@Component
@Slf4j
public class AllCyclePlugin {
    /**
     * 平原时间
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALL_CYCLE_CMD)
    public void allCycleHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postAllCycleImage", Codes.WARFRAME_ALL_CYCLE_PLUGIN, log);
    }
}

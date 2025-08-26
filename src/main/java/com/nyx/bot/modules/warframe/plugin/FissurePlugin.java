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
 * 裂隙
 */
@Shiro
@Component
@Slf4j
public class FissurePlugin {
    /**
     * 裂隙
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_CMD)
    public void activeMissionHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.sendForCode(bot, event, "postFissuresImage", Codes.WARFRAME_ACTIVE_MISSION_PLUGIN, log);
    }

    /**
     * 钢铁裂隙
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_PATH_CMD)
    public void activeMissionPathHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.sendForCode(bot, event, "postFissuresImage", Codes.WARFRAME_ACTIVE_MISSION_PATH_PLUGIN, log);
    }

    /**
     * 虚空风暴
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_STORMS_CMD)
    public void steelPathHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.sendForCode(bot, event, "postFissuresImage", Codes.WARFRAME_VOID_STORMS_PLUGIN, log);
    }
}

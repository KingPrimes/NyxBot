package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Shiro
@Component
@Slf4j
public class KnownCalendarSeasonsPlugin {

    private final WorldStateUtils worldStateUtils;

    private final DrawImagePlugin drawImagePlugin;

    public KnownCalendarSeasonsPlugin(WorldStateUtils worldStateUtils, DrawImagePlugin drawImagePlugin) {
        this.worldStateUtils = worldStateUtils;
        this.drawImagePlugin = drawImagePlugin;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_KNOWN_CALENDAR_SEASONS_CMD, at = AtEnum.BOTH)
    public void knownCalendarSeasonsHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postKnownCalendarSeasonsImage(), Codes.WARFRAME_KNOWN_CALENDAR_SEASONS_PLUGIN, log);
    }


    private byte[] postKnownCalendarSeasonsImage() throws DataNotInfoException {
        return drawImagePlugin.drawKnownCalendarSeasonsImage(worldStateUtils.getKnownCalendarSeasons());
    }


}

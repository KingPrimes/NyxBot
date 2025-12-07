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
import com.nyx.bot.modules.warframe.enums.FissureTypeEnum;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.worldstate.ActiveMission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 裂隙
 */
@Shiro
@Component
@Slf4j
public class FissurePlugin {
    private final DrawImagePlugin drawImagePlugin;

    private final WorldStateUtils worldStateUtils;


    public FissurePlugin(DrawImagePlugin drawImagePlugin, WorldStateUtils worldStateUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.worldStateUtils = worldStateUtils;
    }

    /**
     * 裂隙
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_CMD, at = AtEnum.BOTH)
    public void activeMissionHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postFissuresImage(FissureTypeEnum.ACTIVE_MISSION), Codes.WARFRAME_ACTIVE_MISSION_PLUGIN, log);
    }

    /**
     * 钢铁裂隙
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_PATH_CMD, at = AtEnum.BOTH)
    public void activeMissionPathHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postFissuresImage(FissureTypeEnum.STEEL_PATH), Codes.WARFRAME_ACTIVE_MISSION_PATH_PLUGIN, log);
    }

    /**
     * 虚空风暴
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_STORMS_CMD, at = AtEnum.BOTH)
    public void steelPathHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postFissuresImage(FissureTypeEnum.VOID_STORMS), Codes.WARFRAME_VOID_STORMS_PLUGIN, log);
    }

    private byte[] postFissuresImage(FissureTypeEnum type) throws DataNotInfoException {
        List<ActiveMission> fissure = worldStateUtils.getFissure(type);
        return drawImagePlugin.drawActiveMissionImage(fissure);
    }
}

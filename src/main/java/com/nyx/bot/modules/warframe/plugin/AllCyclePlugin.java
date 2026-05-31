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
import io.github.kingprimes.DrawImagePlugin;
import org.springframework.stereotype.Component;

/**
 * 平原时间
 */
@Shiro
@Component
public class AllCyclePlugin extends AbstractWorldStatePlugin {

    public AllCyclePlugin(DrawImagePlugin drawImagePlugin, WorldStateUtils worldStateUtils) {
        super(drawImagePlugin, worldStateUtils);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALL_CYCLE_CMD, at = AtEnum.BOTH)
    public void allCycleHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        sendImage(bot, event);
    }

    @Override
    protected byte[] getImage() throws DataNotInfoException {
        return drawImagePlugin.drawAllCycleImage(worldStateUtils.getAllCycle());
    }

    @Override
    protected Codes getCode() {
        return Codes.WARFRAME_ALL_CYCLE_PLUGIN;
    }
}

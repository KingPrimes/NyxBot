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
import com.nyx.bot.utils.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 平原时间
 */
@Shiro
@Component
@Slf4j
public class AllCyclePlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @Resource
    WorldStateUtils worldStateUtils;

    /**
     * 平原时间
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALL_CYCLE_CMD, at = AtEnum.BOTH)
    public void allCycleHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, getAllCycleImage(), Codes.WARFRAME_ALL_CYCLE_PLUGIN, log);
    }

    private byte[] getAllCycleImage() throws DataNotInfoException {
        return drawImagePlugin.drawAllCycleImage(worldStateUtils.getAllCycle());
    }
}

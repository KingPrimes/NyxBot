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
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 突击
 */
@Shiro
@Component
@Slf4j
public class SortiesPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @Resource
    WorldStateUtils worldStateUtils;


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SORTIES_CMD, at = AtEnum.BOTH)
    public void sortiesHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postAssaultImage(), Codes.WARFRAME_SORTIES_PLUGIN, log);
    }

    private byte[] postAssaultImage() throws DataNotInfoException {
        return drawImagePlugin.drawSortiesImage(worldStateUtils.getSorties().getFirst());
    }
}

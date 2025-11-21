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
 * 执刑官猎杀
 */
@Shiro
@Component
@Slf4j
public class LiteSoritePlugin {
    @Resource
    WorldStateUtils worldStateUtils;

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_LITE_SORITE_CMD, at = AtEnum.BOTH)
    public void liteSoriteHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postLiteSoriteImage(), Codes.WARFRAME_LITE_SORITE_PLUGIN, log);
    }

    private byte[] postLiteSoriteImage() throws DataNotInfoException {
        return drawImagePlugin.drawLiteSoriteImage(worldStateUtils.getLiteSorite().getFirst());
    }
}

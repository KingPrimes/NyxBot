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
 * 奸商
 */
@Shiro
@Component
@Slf4j
public class VoidPlugin {


    @Resource
    WorldStateUtils worldStateUtils;

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_CMD, at = AtEnum.BOTH)
    public void voidHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, postVoidImage(), Codes.WARFRAME_VOID_PLUGIN, log);
    }

    private byte[] postVoidImage() throws DataNotInfoException {
        return drawImagePlugin.drawVoidTraderImage(worldStateUtils.getVoidTraders());
    }
}

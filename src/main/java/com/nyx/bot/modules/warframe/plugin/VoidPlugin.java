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
 * 奸商
 */
@Shiro
@Component
public class VoidPlugin extends AbstractWorldStatePlugin {

    public VoidPlugin(WorldStateUtils worldStateUtils, DrawImagePlugin drawImagePlugin) {
        super(drawImagePlugin, worldStateUtils);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_CMD, at = AtEnum.BOTH)
    public void voidHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        sendImage(bot, event);
    }

    @Override
    protected byte[] getImage() throws DataNotInfoException {
        return drawImagePlugin.drawVoidTraderImage(worldStateUtils.getVoidTraders());
    }

    @Override
    protected Codes getCode() {
        return Codes.WARFRAME_VOID_PLUGIN;
    }
}

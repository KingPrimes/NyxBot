package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractWorldStatePlugin {

    protected final DrawImagePlugin drawImagePlugin;
    protected final WorldStateUtils worldStateUtils;

    protected AbstractWorldStatePlugin(DrawImagePlugin drawImagePlugin, WorldStateUtils worldStateUtils) {
        this.drawImagePlugin = drawImagePlugin;
        this.worldStateUtils = worldStateUtils;
    }

    protected void sendImage(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, getImage(), getCode(), log);
    }

    protected abstract byte[] getImage() throws DataNotInfoException;

    protected abstract Codes getCode();
}

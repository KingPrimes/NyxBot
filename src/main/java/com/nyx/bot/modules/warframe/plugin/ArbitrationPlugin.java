package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.Arbitration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 仲裁
 */
@Shiro
@Component
@Slf4j
public class ArbitrationPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_CMD, at = AtEnum.BOTH)
    public void arbitrationHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event, arbitrationImage(), Codes.WARFRAME_ARBITRATION_PLUGIN, log);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_EX_CMD, at = AtEnum.BOTH)
    public void arbitrationExHandler(Bot bot, AnyMessageEvent event) {
        SendUtils.send(bot, event, postArbitrationExImage(), Codes.WARFRAME_ARBITRATION_EX_PLUGIN, log);
    }


    private byte[] arbitrationImage() throws DataNotInfoException {
        Arbitration arbitration = ArbitrationCache.getArbitration().orElseThrow(() -> new DataNotInfoException("仲裁信息不存在"));
        return drawImagePlugin.drawArbitrationImage(arbitration);
    }

    private byte[] postArbitrationExImage() {
        return drawImagePlugin.drawArbitrationsImage(ArbitrationCache.getArbitrationList());
    }
}

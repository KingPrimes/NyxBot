package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 平原时间
 */
@Shiro
@Component
@Slf4j
public class AllCyclePlugin {
    /**
     * 平原时间
     */
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ALL_CYCLE_CMD,at = AtEnum.BOTH)
    public void allCycleHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, HtmlToImage.generateImage("html/allCycle", () -> {
            ModelMap model = new ModelMap();
            try {
                WorldState ws = WarframeCache.getWarframeStatus();
                model.put("earth", ws.getEarthCycle());
                model.put("cetus", ws.getCetusCycle());
                model.put("vallis", ws.getVallisCycle());
                model.put("cambion", ws.getCambionCycle());
                model.put("zariman", ws.getZarimanCycle());
            } catch (DataNotInfoException e) {
                throw new RuntimeException(e);
            }
            return model;
        }).toByteArray(), Codes.WARFRAME_ALL_CYCLE_PLUGIN, log);
    }
}

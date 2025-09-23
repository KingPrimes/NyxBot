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
import com.nyx.bot.modules.warframe.res.worldstate.SteelPathOffering;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 钢铁奖励轮换
 */
@Shiro
@Component
@Slf4j
public class SteelPathPlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_STEEL_PATH_CMD,at = AtEnum.BOTH)
    public void steelPathHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postSteelPathImage(), Codes.WARFRAME_STEEL_PATH_PLUGIN, log);
    }

    private byte[] postSteelPathImage() throws DataNotInfoException, HtmlToImageException {
        SteelPathOffering steelPath = WarframeCache.getWarframeStatus().getSteelPath();
        return HtmlToImage.generateImage("html/steePath", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("stee", steelPath);
            return modelMap;
        }).toByteArray();
    }
}

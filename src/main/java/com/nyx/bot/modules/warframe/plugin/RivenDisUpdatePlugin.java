package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.repo.RivenTrendRepository;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 紫卡倾向变动
 */
@Shiro
@Component
@Slf4j
public class RivenDisUpdatePlugin {

    @Resource
    RivenTrendRepository repository;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_DIS_UPDATE_CMD,at = AtEnum.BOTH)
    public void rivenDisUpdate(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postRivenDispositionUpdatesImage(), Codes.WARFRAME_RIVEN_DIS_UPDATE_PLUGIN, log);
    }

    private byte[] postRivenDispositionUpdatesImage() throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/rivenDispositionUpdates", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("data", repository.findRivenDisUpdate());
            return modelMap;
        }).toByteArray();
    }
}

package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.res.worldstate.DuviriCycle;
import com.nyx.bot.modules.warframe.res.worldstate.EndlessXpChoices;
import com.nyx.bot.modules.warframe.service.TranslationService;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 双衍王境 轮换
 */
@Shiro
@Component
@Slf4j
public class DuviriCyclePlugin {

    @Resource
    TranslationService repository;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_DUVIRI_CYCLE_CMD)
    public void duviriCycle(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postDuviriCycleImage(), Codes.WARFRAME_DUVIRI_CYCLE, log);
    }

    private byte[] postDuviriCycleImage() throws DataNotInfoException, HtmlToImageException {
        DuviriCycle duviriCycle = WarframeCache.getWarframeStatus().getDuviriCycle();
        List<EndlessXpChoices> list = duviriCycle.getChoices().stream().peek(c -> {
            if (c.getCategory().equals(EndlessXpChoices.Category.EXC_HARD)) {
                c.setChoices(c.getChoices().stream().map(s -> repository.enToZh(s)).toList());
            }
        }).toList();
        duviriCycle.setChoices(list);
        return HtmlToImage.generateImage("html/duviriCycle", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("duiri", duviriCycle);
            return modelMap;
        }).toByteArray();
    }
}

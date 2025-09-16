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
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.res.worldstate.DailyDeals;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 每日特惠
 */
@Shiro
@Component
@Slf4j
public class DailyDealsPlugin {
    @Resource
    StateTranslationRepository repository;


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_DAILY_DEALS_CMD,at = AtEnum.BOTH)
    public void dailyDealsHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postDailyDealsImage(), Codes.WARFRAME_DAILY_DEALS_PLUGIN, log);
    }

    private byte[] postDailyDealsImage() throws DataNotInfoException, HtmlToImageException {
        List<DailyDeals> dailyDeals = WarframeCache.getWarframeStatus().getDailyDeals()
                .stream()
                .peek(i -> repository.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName())))
                .toList();
        return HtmlToImage.generateImage("html/daily", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("deals", dailyDeals);
            return modelMap;
        }).toByteArray();
    }
}

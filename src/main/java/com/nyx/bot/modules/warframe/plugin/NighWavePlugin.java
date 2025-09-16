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
import com.nyx.bot.modules.warframe.repo.exprot.NightWaveRepository;
import com.nyx.bot.modules.warframe.res.worldstate.SeasonInfo;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 电波
 */
@Shiro
@Component
@Slf4j
public class NighWavePlugin {


    @Resource
    NightWaveRepository repository;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_NIGH_WAVE_CMD,at = AtEnum.BOTH)
    public void nighWave(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postNighWaveImage(), Codes.WARFRAME_NIGH_WAVE_PLUGIN, log);
    }

    private byte[] postNighWaveImage() throws DataNotInfoException, HtmlToImageException {
        SeasonInfo seasonInfo = WarframeCache.getWarframeStatus().getSeasonInfo();
        seasonInfo.setActiveChallenges(seasonInfo.getActiveChallenges().stream().peek(c -> repository.findById(c.getChallenge()).ifPresent(c::setNightwave)).toList());
        return HtmlToImage.generateImage("html/nighWave", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("nigh", seasonInfo);
            return modelMap;
        }).toByteArray();
    }
}

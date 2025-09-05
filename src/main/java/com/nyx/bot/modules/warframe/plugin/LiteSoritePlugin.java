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
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.LiteSorite;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 执刑官猎杀
 */
@Shiro
@Component
@Slf4j
public class LiteSoritePlugin {
    @Resource
    NodesRepository node;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_LITE_SORITE_CMD)
    public void liteSoriteHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postLiteSoriteImage(), Codes.WARFRAME_LITE_SORITE_PLUGIN, log);
    }

    private byte[] postLiteSoriteImage() throws DataNotInfoException, HtmlToImageException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<LiteSorite> liteSorties = sgs.getLiteSorties().stream()
                .peek(s -> s.setMissions(s.getMissions().stream()
                        .peek(v -> node.findById(v.getNode())
                                .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"))).toList())).toList();
        return HtmlToImage.generateImage("html/arsonHunt", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("arsonHunt", liteSorties);
            return modelMap;
        }).toByteArray();
    }
}

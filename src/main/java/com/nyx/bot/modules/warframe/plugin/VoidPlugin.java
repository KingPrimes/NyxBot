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
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.worldstate.VoidTrader;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 奸商
 */
@Shiro
@Component
@Slf4j
public class VoidPlugin {

    @Resource
    StateTranslationRepository str;


    @Resource
    NodesRepository nodesRepository;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_CMD)
    public void voidHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postVoidImage(), Codes.WARFRAME_VOID_PLUGIN, log);
    }

    private byte[] postVoidImage() throws DataNotInfoException, HtmlToImageException {
        List<VoidTrader> list = WarframeCache.getWarframeStatus().getVoidTraders().stream()
                .peek(v -> {
                    nodesRepository.findById(v.getNode())
                            .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                    if (v.getManifest() != null && !v.getManifest().isEmpty()) {
                        v.setManifest(v.getManifest()
                                .stream()
                                .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName())))
                                .toList()
                        );
                    }
                }).limit(1).toList();
        return HtmlToImage.generateImage("html/voidTrader", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("vo", list);
            return modelMap;
        }).toByteArray();
    }
}

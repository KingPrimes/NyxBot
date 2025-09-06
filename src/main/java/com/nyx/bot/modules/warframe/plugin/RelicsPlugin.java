package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.entity.exprot.Relics;
import com.nyx.bot.modules.warframe.service.RelicsService;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 遗物查询
 */
@Shiro
@Component
@Slf4j
public class RelicsPlugin {

    @Resource
    RelicsService relicsService;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RELICS_CMD)
    public void relics(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        String key = event.getRawMessage().replaceAll(CommandConstants.WARFRAME_RELICS_CMD, "").trim();
        if (key.toLowerCase().contains("forma")) {
            bot.sendMsg(event, "遗物查询不支持Forma类遗物", false);
            return;
        }
        SendUtils.send(bot, event, postRelicsImage(key), Codes.WARFRAME_RELICS_PLUGIN, log);
    }

    private byte[] postRelicsImage(String key) throws DataNotInfoException, HtmlToImageException {
        List<Relics> relics = relicsService.findAllByRelicNameOrRewardsItemName(key);
        return HtmlToImage.generateImage("html/relics", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("relics", relics);
            return modelMap;
        }).toByteArray();
    }
}

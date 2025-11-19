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
import com.nyx.bot.modules.warframe.service.RelicsService;
import com.nyx.bot.utils.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.Relics;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 遗物查询
 */
@Shiro
@Component
@Slf4j
public class RelicsPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @Resource
    RelicsService relicsService;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RELICS_CMD, at = AtEnum.BOTH)
    public void relics(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        String key = event.getRawMessage().replaceAll(CommandConstants.WARFRAME_RELICS_CMD, "").trim();
        if (key.toLowerCase().contains("forma")) {
            bot.sendMsg(event, "遗物查询不支持Forma类遗物", false);
            return;
        }
        SendUtils.send(bot, event, postRelicsImage(key), Codes.WARFRAME_RELICS_PLUGIN, log);
    }

    private byte[] postRelicsImage(String key) {
        List<io.github.kingprimes.model.Relics> relics = relicsService.findAllByRelicNameOrRewardsItemName(key)
                .stream()
                .map(sr -> new Relics()
                        .setName(sr.getName())
                        .setRewards(sr.getRelicRewards()
                                .stream()
                                .map(rs -> new Relics.Rewards()
                                        .setName(rs.getRewardName())
                                        .setRarity(rs.getRarity())
                                        .setItemCount(rs.getItemCount()))
                                .toList()))
                .toList();
        return drawImagePlugin.drawRelicsImage(relics);
    }
}

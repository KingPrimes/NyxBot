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
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.worldstate.Invasion;
import com.nyx.bot.modules.warframe.res.worldstate.Reward;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Objects;

/**
 * 入侵
 */
@Shiro
@Component
@Slf4j
public class InvasionPlugin {
    @Resource
    NodesRepository node;
    @Resource
    StateTranslationRepository str;


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_INVASIONS_CMD,at = AtEnum.BOTH)
    public void invasionHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postInvasionsImage(), Codes.WARFRAME_INVASIONS_PLUGIN, log);
    }

    private byte[] postInvasionsImage() throws DataNotInfoException, HtmlToImageException {
        List<Invasion> list = getInvasions(WarframeCache.getWarframeStatus().getInvasions());
        return HtmlToImage.generateImage("html/invasions", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("inv", list);
            return modelMap;
        }).toByteArray();
    }

    private List<Invasion> getInvasions(List<Invasion> invasions) {
        return invasions.stream()
                .filter(i -> !i.getCompleted())
                .peek(d -> {
                            node.findById(d.getNode())
                                    .ifPresent(nodes -> d.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            List<Reward.Item> items = d.getDefenderReward().getCountedItems()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName())))
                                    .toList();
                            d.getDefenderReward().setCountedItems(items);

                            d.setAttackerReward(d.getAttackerReward().stream()
                                    .filter(Objects::nonNull)
                                    .peek(r -> r.setCountedItems(
                                            r.getCountedItems()
                                                    .stream()
                                                    .filter(Objects::nonNull)
                                                    .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName())))
                                                    .toList()
                                    )).toList());
                        }
                ).toList();
    }
}

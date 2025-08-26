package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.worldstate.Invasion;
import com.nyx.bot.modules.warframe.res.worldstate.Reward;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Objects;

/**
 * 入侵
 */
@Controller
@RequestMapping("/private")
public class InvasionsHtmlController {
    @Resource
    StateTranslationRepository str;
    @Resource
    NodesRepository nodesRepository;

    @GetMapping("/getInvasionsHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        List<Invasion> list = getInvasions(WarframeCache.getWarframeStatus().getInvasions());
        model.addAttribute("inv", list);
        return "html/invasions";
    }

    @PostMapping("/postSubscribeInvasionsHtml")
    public String postSubscribeFissuresHtml(Model model, @RequestBody List<Invasion> invasions) {
        model.addAttribute("inv", getInvasions(invasions));
        return "html/invasions";
    }

    private List<Invasion> getInvasions(List<Invasion> invasions) {
        return invasions.stream()
                .filter(i -> !i.getCompleted())
                .peek(d -> {
                            nodesRepository.findById(d.getNode())
                                    .ifPresent(nodes -> d.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            List<Reward.Item> items = d.getDefenderReward().getCountedItems()
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .peek(i -> {
                                        str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                                    })
                                    .toList();
                            d.getDefenderReward().setCountedItems(items);

                            d.setAttackerReward(d.getAttackerReward().stream()
                                    .filter(Objects::nonNull)
                                    .peek(r -> {
                                        r.setCountedItems(
                                                r.getCountedItems()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .peek(i -> {
                                                            str.findByUniqueName(StringUtils.getLastThreeSegments(i.getName())).ifPresent(s -> i.setName(s.getName()));
                                                        })
                                                        .toList()
                                        );
                                    }).toList());
                        }
                ).toList();
    }
}

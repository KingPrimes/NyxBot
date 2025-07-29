package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.warframe.exprot.NodesRepository;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.res.worldstate.LiteSorite;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 执刑官猎杀
 */
@Controller
@RequestMapping("/private")
public class ArsonHuntHtmlController {

    @Resource
    NodesRepository nodesRepository;

    @GetMapping("/getArsonHuntHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<LiteSorite> liteSorties = sgs.getLiteSorties().stream()
                .peek(s -> {
                    s.setMissions(s.getMissions().stream()
                            .peek(v -> {
                                nodesRepository.findById(v.getNode())
                                        .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            }).toList());
                }).toList();
        model.addAttribute("arsonHunt", liteSorties);
        return "html/arsonHunt";
    }
}

package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.Sortie;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 突击
 */
@Controller
@RequestMapping("/private")
public class AssaultHtmlController {
    @Resource
    NodesRepository nodesRepository;

    @GetMapping("/getAssaultHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<Sortie> sorties = sgs.getSorties().stream()
                .peek(s -> {
                    s.setVariants(s.getVariants().stream()
                            .peek(v -> {
                                nodesRepository.findById(v.getNode())
                                        .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                            }).toList());
                }).toList();
        model.addAttribute("assault", sorties);
        return "html/assault";
    }
}

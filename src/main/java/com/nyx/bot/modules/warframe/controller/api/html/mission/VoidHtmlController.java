package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.repo.StateTranslationRepository;
import com.nyx.bot.modules.warframe.repo.exprot.NodesRepository;
import com.nyx.bot.modules.warframe.res.worldstate.VoidTrader;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 奸商
 */
@Controller
@RequestMapping("/private")
public class VoidHtmlController {

    @Resource
    StateTranslationRepository str;


    @Resource
    NodesRepository nodesRepository;

    @GetMapping("/getVoidHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        List<VoidTrader> list = WarframeCache.getWarframeStatus().getVoidTraders().stream()
                .peek(v -> {
                    nodesRepository.findById(v.getNode())
                            .ifPresent(nodes -> v.setNode(nodes.getName() + "(" + nodes.getSystemName() + ")"));
                    if (v.getManifest() != null && !v.getManifest().isEmpty()) {
                        v.setManifest(v.getManifest()
                                .stream()
                                .peek(i -> {
                                    str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName()));
                                })
                                .toList()
                        );
                    }
                }).limit(1).toList();
        model.addAttribute("vo", list);
        return "html/voidTrader";
    }
}

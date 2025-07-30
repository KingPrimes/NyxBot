package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.warframe.exprot.NightwaveRepository;
import com.nyx.bot.res.worldstate.SeasonInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class NighTwaveHtmlController {

    @Resource
    NightwaveRepository nightwaveRepository;

    @GetMapping("/getNighTwaveHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        SeasonInfo seasonInfo = WarframeCache.getWarframeStatus().getSeasonInfo();
        seasonInfo.setActiveChallenges(seasonInfo.getActiveChallenges().stream().peek(c -> {
            nightwaveRepository.findById(c.getChallenge()).ifPresent(c::setNightwave);
        }).toList());
        model.addAttribute("nigh", seasonInfo);
        return "html/nighTwave";
    }
}

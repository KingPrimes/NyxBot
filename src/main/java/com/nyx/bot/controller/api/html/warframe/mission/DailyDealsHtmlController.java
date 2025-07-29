package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.warframe.StateTranslationRepository;
import com.nyx.bot.res.worldstate.DailyDeals;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 每日特惠
 */
@Controller
@RequestMapping("/private")
public class DailyDealsHtmlController {

    @Resource
    StateTranslationRepository str;


    @GetMapping("/getDailyDealsHtml")
    public String getHtml(Model model) throws DataNotInfoException {

        List<DailyDeals> dailyDeals = WarframeCache.getWarframeStatus().getDailyDeals().stream()
                .peek(i -> str.findByUniqueName(StringUtils.getLastThreeSegments(i.getItem())).ifPresent(s -> i.setItem(s.getName())))
                .toList();
        model.addAttribute("deals", dailyDeals);
        return "html/daily";
    }
}

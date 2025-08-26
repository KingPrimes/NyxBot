package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.res.worldstate.DuviriCycle;
import com.nyx.bot.modules.warframe.res.worldstate.EndlessXpChoices;
import com.nyx.bot.modules.warframe.service.TranslationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 双衍王境
 */
@Controller
@RequestMapping("/private")
@Slf4j
public class DuviriCycleController {


    @Resource
    TranslationService trans;

    @GetMapping("/getDuviriCycleHtml")
    public String getHtml(Model model) throws DataNotInfoException {
        DuviriCycle duviriCycle = WarframeCache.getWarframeStatus().getDuviriCycle();
        List<EndlessXpChoices> list = duviriCycle.getChoices().stream().peek(c -> {
            if (c.getCategory().equals(EndlessXpChoices.Category.EXC_HARD)) {
                c.setChoices(c.getChoices().stream().map(s -> trans.enToZh(s)).toList());
            }
        }).toList();
        duviriCycle.setChoices(list);
        model.addAttribute("duiri", duviriCycle);
        return "html/duviriCycle";
    }

}

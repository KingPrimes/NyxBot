package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class NighTwaveHtmlController {
    @Resource
    TranslationService trans;

    @GetMapping("/getNighTwaveHtml")
    public String getHtml(Model model) throws DataNotInfoException {
//        GlobalStates sgs = CacheUtils.getGlobalState();
//        GlobalStates.Nightwave nightwave = sgs.getNightwave();
//        //翻译
//        nightwave.getActiveChallenges().forEach(c -> c.setDesc(trans.enToZh(c.getDesc())));
//        //排序 从小到大
//        nightwave.getActiveChallenges().sort(Comparator.comparing(GlobalStates.Nightwave.ActiveChallenges::getReputation));
//
//        model.addAttribute("nigh", nightwave.getActiveChallenges());

        return "html/nighTwave";
    }
}

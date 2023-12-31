package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Comparator;

@Controller
@RequestMapping("/private")
public class NighTwaveHtmlController {
    @Autowired
    TranslationService trans;

    @GetMapping("/getNighTwaveHtml")
    public String getHtml(Model model) {
        SocketGlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates data = sgs.getPacket().getData();
        GlobalStates.Nightwave nightwave = data.getNightwave();
        //翻译
        nightwave.getActiveChallenges().forEach(c -> {
            c.setDesc(trans.enToZh(c.getDesc()));
        });
        //排序 从小到大
        nightwave.getActiveChallenges().sort(Comparator.comparing(GlobalStates.Nightwave.ActiveChallenges::getReputation));

        model.addAttribute("nigh", nightwave.getActiveChallenges());

        return "html/nighTwave";
    }
}

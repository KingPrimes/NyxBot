package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * 执刑官猎杀
 */
@Controller
@RequestMapping("/private")
public class ArsonHuntHtmlController {
    @Resource
    TranslationService trans;

    @GetMapping("/getArsonHuntHtml")
    public String getHtml(Model model) {
        GlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.ArchonHunt archonHunt = sgs.getArchonHunt();
        for (GlobalStates.ArchonHunt.Mission mission : archonHunt.getMissions()) {
            mission.setNode(mission.getNode()
                    .replace(
                            StringUtils.quStr(
                                    mission.getNode()),
                            trans.enToZh(StringUtils.quStr(mission.getNode()))));
            mission.setType(trans.enToZh(mission.getType()));
        }
        archonHunt.setEta(DateUtils.getDiff((archonHunt.getExpiry()), new Date(), true));
        model.addAttribute("arsonHunt", archonHunt);
        return "html/arsonHunt";
    }
}

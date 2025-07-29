package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 钢铁
 */
@Controller
@RequestMapping("/private")
public class SteelPathHtmlController {
    @Resource
    TranslationService trans;

    @GetMapping("/getSteelPathHtml")
    public String getHtml(Model model) throws DataNotInfoException {
//        GlobalStates sgs = CacheUtils.getGlobalState();
//        GlobalStates.SteelPath steelPath = sgs.getSteelPath();
//        String key = "";
//        for (int i = 0; i < steelPath.getRotation().size(); i++) {
//            if (steelPath.getCurrentReward().getName().equals(steelPath.getRotation().get(i).getName())) {
//                if (i + 1 < steelPath.getRotation().size()) {
//                    key = steelPath.getRotation().get(i + 1).getName();
//                } else {
//                    key = steelPath.getRotation().get(0).getName();
//                }
//                break;
//            }
//        }
//        steelPath.setNexReward(trans.enToZh(key));
//        steelPath.setIsReward(trans.enToZh(steelPath.getCurrentReward().getName()));
//        steelPath.setEtc(DateUtils.getDateWeek(steelPath.getActivation(), new Date(), Calendar.DAY_OF_MONTH, 7));
//        steelPath.setCost(steelPath.getCurrentReward().getCost());
//        model.addAttribute("stee", steelPath);
        return "html/steePath";
    }
}

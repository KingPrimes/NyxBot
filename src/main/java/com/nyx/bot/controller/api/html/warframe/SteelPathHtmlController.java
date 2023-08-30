package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Calendar;
import java.util.Date;

/**
 * 钢铁
 */
@Controller
@RequestMapping("/private")
public class SteelPathHtmlController {
    @Autowired
    TranslationService trans;

    @GetMapping("/getSteelPathHtml")
    public String getHtml(Model model) {
        SocketGlobalStates sgs = CacheUtils.getGlobalState();
        GlobalStates.SteelPath steelPath = sgs.getPacket().getData().getSteelPath();
        String key = "";
        for (int i = 0; i < steelPath.getRotation().size(); i++) {
            if (steelPath.getCurrentReward().getName().equals(steelPath.getRotation().get(i).getName())) {
                if (i + 1 < steelPath.getRotation().size()) {
                    key = steelPath.getRotation().get(i + 1).getName();
                } else {
                    key = steelPath.getRotation().get(0).getName();
                }
                break;
            }
        }
        steelPath.setNexReward(trans.enToZh(key));
        steelPath.setIsReward(trans.enToZh(steelPath.getCurrentReward().getName()));
        steelPath.setEtc(DateUtils.getDateWeek(steelPath.getActivation(), new Date(), Calendar.DAY_OF_MONTH, 7));
        steelPath.setCost(steelPath.getCurrentReward().getCost());
        model.addAttribute("stee", steelPath);
        return "html/steePath";
    }
}

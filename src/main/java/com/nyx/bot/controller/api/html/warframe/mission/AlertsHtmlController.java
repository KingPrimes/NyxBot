package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;
import java.util.List;

/**
 * 警报
 */
@Controller
@RequestMapping("/private")
public class AlertsHtmlController {
    @Resource
    TranslationService trans;

    @GetMapping("/getAlertsHtml")
    public String getHtml(ModelMap model) throws DataNotInfoException {
        GlobalStates sgs = CacheUtils.getGlobalState();
        List<GlobalStates.Alerts> alerts = sgs.getAlerts();
        if (alerts.isEmpty()) {
            model.put("alerts", null);
        }
        alerts.forEach(alert -> {
            GlobalStates.Alerts.Mission mission = alert.getMission();
            mission.setNode(mission.getNode().
                    replace(
                            StringUtils.quStr(mission.getNode()),
                            trans.enToZh(StringUtils.quStr(mission.getNode())
                            )
                    ));
            mission.setType(trans.enToZh(mission.getType()));
            GlobalStates.Alerts.Mission.Reward reward = mission.getReward();
            reward.getCountedItems().forEach(r -> r.setKey(trans.enToZh(r.getKey())));
            alert.setEta(DateUtils.getDiff((alert.getExpiry()), new Date(), true));
        });
        model.put("alerts", alerts);
        return "html/alerts";
    }

    @PostMapping("/postSubscribeAlertsHtml")
    public String postSubscribeFissuresHtml(Model model, @RequestBody List<GlobalStates.Alerts> alerts) {
        alerts.forEach(alert -> {
            GlobalStates.Alerts.Mission mission = alert.getMission();
            mission.setNode(mission.getNode().
                    replace(
                            StringUtils.quStr(mission.getNode()),
                            trans.enToZh(StringUtils.quStr(mission.getNode())
                            )
                    ));
            mission.setType(trans.enToZh(mission.getType()));
            GlobalStates.Alerts.Mission.Reward reward = mission.getReward();
            reward.getCountedItems().forEach(r -> r.setKey(trans.enToZh(r.getKey())));
            alert.setEta(DateUtils.getDiff((alert.getExpiry()), new Date(), true));
        });
        model.addAttribute("alerts", alerts);
        return "html/alerts";
    }
}

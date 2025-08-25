package com.nyx.bot.modules.warframe.controller.api.html.mission;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.Alert;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 警报
 */
@Controller
@RequestMapping("/private")
public class AlertHtmlController {

    @GetMapping("/getAlertsHtml")
    public String getHtml(ModelMap model) throws DataNotInfoException {
        WorldState sgs = WarframeCache.getWarframeStatus();
        List<Alert> alerts = sgs.getAlerts();
        if (alerts.isEmpty()) {
            model.put("alerts", null);
        }
//        getAlerts(alerts);
//        model.put("alerts", alerts);
        return "html/alerts";
    }

//    private void getAlerts(List<GlobalStates.Alerts> alerts) {
//        alerts.forEach(alert -> {
//            GlobalStates.Alerts.Mission mission = alert.getMission();
//            mission.setNode(mission.getNode().
//                    replace(
//                            StringUtils.quStr(mission.getNode()),
//                            trans.enToZh(StringUtils.quStr(mission.getNode())
//                            )
//                    ));
//            mission.setType(trans.enToZh(mission.getType()));
//            GlobalStates.Alerts.Mission.Reward reward = mission.getReward();
//            reward.getCountedItems().forEach(r -> r.setKey(trans.enToZh(r.getKey())));
//            alert.setEta(DateUtils.getDiff((alert.getExpiry()), new Date(), true));
//        });
//    }

    @PostMapping("/postSubscribeAlertsHtml")
    public String postSubscribeFissuresHtml(Model model, @RequestBody List<Alert> alerts) {
        //getAlerts(alerts);
        model.addAttribute("alerts", alerts);
        return "html/alerts";
    }
}

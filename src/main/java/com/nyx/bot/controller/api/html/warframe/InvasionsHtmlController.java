package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 入侵
 */
@Controller
@RequestMapping("/private")
public class InvasionsHtmlController {
    @Resource
    TranslationService trans;

    @GetMapping("/getInvasionsHtml")
    public String getHtml(Model model) {
        GlobalStates sgs = CacheUtils.getGlobalState();
        List<GlobalStates.Invasions> invasions = sgs.getInvasions();
        for (GlobalStates.Invasions invasion : invasions) {
            if (!invasion.getAttackerReward().getCountedItems().isEmpty()) {

                GlobalStates.Invasions.AttackerReward reward = new GlobalStates.Invasions.AttackerReward();
                reward.setCredits(invasion.getDefenderReward().getCredits());
                reward.setColor(invasion.getDefenderReward().getColor());
                reward.setThumbnail(invasion.getDefenderReward().getThumbnail());
                reward.setAsString(invasion.getDefenderReward().getAsString());
                List<GlobalStates.Invasions.AttackerReward.CountedItems> countedItems = new ArrayList<>();

                for (GlobalStates.Invasions.AttackerReward.CountedItems countedItem : invasion.getAttackerReward().getCountedItems()) {
                    countedItem.setType(trans.enToZh(countedItem.getType()));
                    countedItems.add(countedItem);
                }

                reward.setCountedItems(countedItems);
                invasion.setAttackerReward(reward);

            }
            if (!invasion.getDefenderReward().getCountedItems().isEmpty()) {
                GlobalStates.Invasions.DefenderReward defenderReward = new GlobalStates.Invasions.DefenderReward();
                defenderReward.setCredits(invasion.getDefenderReward().getCredits());
                defenderReward.setColor(invasion.getDefenderReward().getColor());
                defenderReward.setThumbnail(invasion.getDefenderReward().getThumbnail());
                defenderReward.setAsString(invasion.getDefenderReward().getAsString());
                List<GlobalStates.Invasions.DefenderReward.CountedItems> countedItems = new ArrayList<>();

                for (GlobalStates.Invasions.DefenderReward.CountedItems countedItem : invasion.getDefenderReward().getCountedItems()) {
                    countedItem.setType(trans.enToZh(countedItem.getType()));
                    countedItems.add(countedItem);
                }

                defenderReward.setCountedItems(countedItems);
                invasion.setDefenderReward(defenderReward);


            }
            invasion.setNode(invasion.getNode().replace(StringUtils.quStr(invasion.getNode()), trans.enToZh(StringUtils.quStr(invasion.getNode()))));
            invasion.setCompletion(String.format("%.2f", Double.valueOf(invasion.getCompletion())));
            invasion.setDesc(trans.enToZh(invasion.getDesc()));
        }
        model.addAttribute("inv", invasions);
        return "html/invasions";
    }
}

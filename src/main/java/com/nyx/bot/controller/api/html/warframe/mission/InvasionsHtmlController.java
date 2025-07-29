package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.TranslationService;
import com.nyx.bot.res.worldstate.Invasion;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String getHtml(Model model) throws DataNotInfoException {
//        GlobalStates sgs = CacheUtils.getGlobalState();
//        List<GlobalStates.Invasions> invasions = sgs.getInvasions();
//        model.addAttribute("inv", getInvasions(invasions));
        return "html/invasions";
    }

    @PostMapping("/postSubscribeInvasionsHtml")
    public String postSubscribeFissuresHtml(Model model, @RequestBody List<Invasion> invasions) {
        //model.addAttribute("inv", getInvasions(invasions));
        return "html/invasions";
    }

//    private List<GlobalStates.Invasions> getInvasions(List<GlobalStates.Invasions> invasions) {
//        List<GlobalStates.Invasions> newInvasions = new ArrayList<>();
//        for (GlobalStates.Invasions invasion : invasions) {
//            //忽略以结束得数据
//            if (invasion.getCompleted()) {
//                continue;
//            }
//            if (!invasion.getVsInfestation()) {
//                if (!invasion.getAttacker().getReward().getCountedItems().isEmpty()) {
//                    GlobalStates.Invasions.RewardInfo reward = invasion.getAttacker();
//                    List<GlobalStates.Invasions.Reward.CountedItems> countedItems = reward.getReward().getCountedItems()
//                            .stream()
//                            //使用数据流得方式替换元素中得内容
//                            .peek(item -> item.setType(trans.enToZh(item.getType())))
//                            .toList();
//                    reward.getReward().setColo(Integer.toHexString(reward.getReward().getColor()));
//                    reward.getReward().setCountedItems(countedItems);
//                }
//            }
//            if (!invasion.getDefender().getReward().getCountedItems().isEmpty()) {
//                GlobalStates.Invasions.RewardInfo defender = invasion.getDefender();
//                List<GlobalStates.Invasions.Reward.CountedItems> countedItems = defender.getReward().getCountedItems()
//                        .stream()
//                        //使用数据流得方式替换元素中得内容
//                        .peek(item -> item.setType(trans.enToZh(item.getType())))
//                        .toList();
//                defender.getReward().setColo(Integer.toHexString(defender.getReward().getColor()));
//                defender.getReward().setCountedItems(countedItems);
//            }
//            invasion.setNode(invasion.getNode().replace(StringUtils.quStr(invasion.getNode()), trans.enToZh(StringUtils.quStr(invasion.getNode()))));
//            invasion.setCompletion(String.format("%.2f", Double.valueOf(invasion.getCompletion())));
//            invasion.setDesc(trans.enToZh(invasion.getDesc()));
//            newInvasions.add(invasion);
//        }
//        return newInvasions;
//    }
}

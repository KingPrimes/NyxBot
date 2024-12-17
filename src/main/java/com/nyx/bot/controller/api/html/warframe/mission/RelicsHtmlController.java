package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.repo.impl.warframe.RelicsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class RelicsHtmlController {

    @Resource
    RelicsService relicsService;

    @PostMapping("/postRelicsHtml")
    public String getHtml(@RequestBody OneBotLogInfoData data, ModelMap model) {
        model.put("rs", relicsService.findAllByRelicNameOrRewardsItemName(data.getData()));
        return "html/relics";
    }

}

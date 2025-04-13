package com.nyx.bot.controller.api.html.warframe.mission;

import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.entity.warframe.Relics;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.RelicsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/private")
public class RelicsHtmlController {

    @Resource
    RelicsService relicsService;

    @PostMapping("/postRelicsHtml")
    public String getHtml(@RequestBody OneBotLogInfoData data, ModelMap model) throws DataNotInfoException {
        List<Relics> relics = relicsService.findAllByRelicNameOrRewardsItemName(data.getData());
        if (!relics.isEmpty()) {
            model.put("rs", relics);
            return "html/relics";
        }
        throw new DataNotInfoException("未找到对应数据<br/>请使用大写字母。");
    }

}

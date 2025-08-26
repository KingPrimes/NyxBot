package com.nyx.bot.modules.warframe.controller.api.html.riven;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Type;
import java.util.List;

@Controller
@RequestMapping("/private")
public class RivenAnalyseTrendHtmlController {

    @PostMapping("/postRivenAnalyseTrend")
    public String getHtml(@RequestBody OneBotLogInfoData data, Model map) {
        List<List<RivenAnalyseTrendModel>> lists = JSON.parseArray(data.getData(), (Type) List.class);
        map.addAttribute("data", lists);
        return "html/rivenAnalyseTrend";
    }

}

package com.nyx.bot.controller.api.html.warframe.riven;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.plugin.warframe.core.RivenAnalyseTrendModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Type;
import java.util.List;

@Controller
@RequestMapping("/private")
public class RivenAnalyseTrendHtmlController {

    @PostMapping("/postRivenAnalyseTrend")
    public String getHtml(@RequestBody OneBotLogInfoData data, ModelMap map) {
        List<List<RivenAnalyseTrendModel>> lists = JSON.parseArray(data.getData(), (Type) List.class);
        map.put("data", lists);
        return "html/rivenAnalyseTrend";
    }

}

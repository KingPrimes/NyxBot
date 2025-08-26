package com.nyx.bot.modules.warframe.controller.api.html.market;

import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
@Slf4j
public class MarketRivenHtmlController {

    @PostMapping("/postMarketRiven")
    public String getHtml(@RequestBody OneBotLogInfoData data, Model model) {
        model.addAttribute("riven", MarketUtils.marketRivenParameter(data.getKey()));
        return "html/marketRiven";
    }

}

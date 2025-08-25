package com.nyx.bot.modules.warframe.controller.api.html.market;

import com.alibaba.fastjson2.JSON;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.res.Ducats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/private")
@Slf4j
public class MarketDucatsHtmlController {
    @PostMapping("/postMarketDucats")
    public String getHtml(@RequestBody OneBotLogInfoData data, Model model) {
        model.addAttribute("code", data.getCodes().equals(Codes.WARFRAME_MARKET_GOD_DUMP));
        Map<String, List<Ducats.Ducat>> map = JSON.parseObject(data.getData(), Map.class);
        model.addAttribute("data", map);
        return "html/marketDucats";
    }
}

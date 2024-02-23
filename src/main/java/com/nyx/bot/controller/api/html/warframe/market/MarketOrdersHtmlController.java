package com.nyx.bot.controller.api.html.warframe.market;

import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.plugin.warframe.utils.MarketUtils;
import com.nyx.bot.res.MarketOrders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/private")
public class MarketOrdersHtmlController {

    @PostMapping("/postMarketOrders")
    public String getHtml(@RequestBody OneBotLogInfoData data, Model model) {
        MarketUtils.Market market = MarketUtils.to(data.getKey());

        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            model.addAttribute("items", market.getPossibleItems());
            return "html/marketPossibleItems";
        }
        // 获取市场订单
        MarketOrders orders = MarketUtils.market(data.getForm().getForm(), market.getKey(), data.getIsBy(), data.getIsMax());

        String id = orders.getInclude().getItem().getId();
        orders.getInclude().getItem().getItemsInSet().stream().filter(item -> item.getId().equals(id)).findFirst().ifPresent(i -> {
            model.addAttribute("ducats", i.getDucats());
            model.addAttribute("level", i.getMasteryLevel());
            model.addAttribute("credits", i.getTradingTax());
            model.addAttribute("type", i.getRarity());
            model.addAttribute("modMax", i.getModMaxRank());
        });
        model.addAttribute("orders", orders.getPayload().getOrders());
        model.addAttribute("itemName", market.getItemName());
        model.addAttribute("form", data.getForm().getForm());
        model.addAttribute("isBy", data.getIsBy());
        model.addAttribute("isMax", data.getIsMax());
        return "html/market";
    }

}

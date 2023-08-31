package com.nyx.bot.controller.api.html.warframe;

import com.nyx.bot.plugin.warframe.utils.MarketUtils;
import com.nyx.bot.res.MarketOrders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/private")
public class MarketOrdersHtmlController {

    @GetMapping("/getMarketOrders/{key}/{form}/{isBy}/{isMax}")
    public String getHtml(@PathVariable String key, @PathVariable String form, @PathVariable Boolean isBy, @PathVariable Boolean isMax, Model model) {
        MarketUtils.Market market = MarketUtils.to(URLDecoder.decode(key, StandardCharsets.UTF_8));

        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            model.addAttribute("items", market.getPossibleItems());
            return "html/marketPossibleItems";
        }
        // 获取市场订单
        MarketOrders orders = MarketUtils.market(form, market.getKey(), isBy, isMax);

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
        model.addAttribute("form", form);
        model.addAttribute("isBy", isBy);
        model.addAttribute("isMax", isMax);
        return "html/market";
    }

}

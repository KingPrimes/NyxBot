package com.nyx.bot.modules.warframe.plugin;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.modules.warframe.res.MarketOrders;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.SendUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 查询 Market 市场订单
 */
@Shiro
@Component
@Slf4j
public class MarketOrdersPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(startWith = {"/WM", "WM", "/市场", "市场", "/wm", "wm"})
    public void marketOrders(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        String str = event.getMessage().trim();
        str = ShiroUtils.unescape(str).toUpperCase();

        if (MatcherUtils.isSpecialSymbols(str)) {
            String orderItem = MatcherUtils.isOrderItem(str);
            if (orderItem.isEmpty()) {
                log.debug("用户:{} 输入了错误的指令:{}", event.getUserId(), str);
                bot.sendMsg(event, "请输入正确的指令！", false);
                return;
            }
            str = orderItem;
        }

        MarketOrdersData marketOrdersData = new MarketOrdersData();

        if (str.contains("满级") || str.contains("MAX")) {
            str = (str.replaceAll("满级", "").replaceAll("MAX", ""));
            marketOrdersData.setMax(true);
        }
        if (str.contains("购买") || str.contains("买家") || str.contains("BUY")) {
            str = (str.replaceAll("购买", "").replaceAll("买家", "").replaceAll("BUY", ""));
            marketOrdersData.setBy(true);
        }

        if (str.contains("出售") || str.contains("卖家") || str.contains("SELL")) {
            str = (str.replaceAll("出售", "").replaceAll("卖家", "").replaceAll("SELL", ""));
            marketOrdersData.setBy(false);
        }
        // 获取平台
        String finalStr = str;
        for (MarketFormEnums form : MarketFormEnums.values()) {
            if (finalStr.contains(form.getForm())) {
                marketOrdersData.setForm(form);
                str = (str.replaceAll(form.getForm(), ""));
                break;
            }
        }

        // 关键字
        marketOrdersData.setKey(str.replaceAll(CommandConstants.WARFRAME_MARKET_ORDERS_CMD, "").trim());
        if (marketOrdersData.getKey().isEmpty()) {
            bot.sendMsg(event, "请输入正确的名称！", false);
            return;
        }
        log.debug("用户:{} 指令 {} 执行参数 {}", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD, JSON.toJSONString(marketOrdersData));

        byte[] bytes = postMarketOrdersImage(marketOrdersData);
        if (bytes.length > 0) {
            bot.sendMsg(event,
                    ArrayMsgUtils.builder().img(bytes).build(), false);
            log.debug("用户:{} 指令 {} 执行成功", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD);
        } else {
            SendUtils.sendErrorMsg(bot, event, Codes.WARFRAME_MARKET_ORDERS_PLUGIN);
            log.debug("用户:{} 指令 {} 执行失败", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD);
        }
    }

    private byte[] postMarketOrdersImage(MarketOrdersData data) throws DataNotInfoException, HtmlToImageException {
        MarketUtils.Market market = MarketUtils.to(data.getKey());
        ModelMap modelMap = new ModelMap();
        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            modelMap.put("items", market.getPossibleItems());
            return HtmlToImage.generateImage("html/marketPossibleItems", () -> modelMap).toByteArray();
        }
        MarketOrders orders = MarketUtils.market(data.getForm().getForm(), market.getKey(), data.isBy(), data.isMax());

        String id = orders.getInclude().getItem().getId();
        orders.getInclude().getItem().getItemsInSet().stream().filter(item -> item.getId().equals(id)).findFirst().ifPresent(i -> {
            modelMap.addAttribute("ducats", i.getDucats());
            modelMap.addAttribute("level", i.getMasteryLevel());
            modelMap.addAttribute("credits", i.getTradingTax());
            modelMap.addAttribute("type", i.getRarity());
            modelMap.addAttribute("modMax", i.getModMaxRank());
        });
        modelMap.addAttribute("orders", orders.getPayload().getOrders());
        modelMap.addAttribute("itemName", market.getItemName());
        modelMap.addAttribute("form", data.getForm().getForm());
        modelMap.addAttribute("isBy", data.isBy());
        modelMap.addAttribute("isMax", data.isMax());

        return HtmlToImage.generateImage("html/market", () -> modelMap).toByteArray();
    }

    @Data
    @Accessors
    static
    class MarketOrdersData {
        private MarketFormEnums form = MarketFormEnums.PC;
        private boolean isBy;
        private boolean isMax;
        private String key;
    }
}

package com.nyx.bot.modules.warframe.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.entity.OrdersItems;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.enums.MarketPlatformEnum;
import io.github.kingprimes.model.market.BaseOrder;
import io.github.kingprimes.model.market.OrderWithUser;
import io.github.kingprimes.model.market.Orders;
import jakarta.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询 Market 市场订单
 */
@Shiro
@Component
@Slf4j
public class MarketOrdersPlugin {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    DrawImagePlugin drawImagePlugin;

    private static Orders getOrders(MarketOrdersData data, BaseOrder<OrderWithUser> order, MarketUtils.Market market) {
        List<OrderWithUser> ows = order.getData();
        OrdersItems oi = market.getItem();
        return new Orders().setName(oi.getName())
                .setForm(data.getForm())
                .setIsBy(data.isBy)
                .setIsMax(data.isMax)
                .setDucats(oi.getDucats())
                .setVaulted(oi.getVaulted())
                .setMaxAmberStars(oi.getMaxAmberStars())
                .setMaxCyanStars(oi.getMaxCyanStars())
                .setBaseEndo(oi.getBaseEndo())
                .setReqMasteryRank(oi.getReqMasteryRank())
                .setTradingTax(oi.getTradingTax())
                .setIcon(null)
                .setOrders(ows);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(startWith = {"/WM", "WM", "/市场", "市场", "/wm", "wm"}, at = AtEnum.BOTH)
    public void marketOrders(Bot bot, AnyMessageEvent event) {
        String str = event.getMessage().trim();
        str = ShiroUtils.unescape(str).toUpperCase();
        log.debug("用户:{} 输入了指令:{}", event.getUserId(), str);
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
        for (MarketPlatformEnum form : MarketPlatformEnum.values()) {
            if (finalStr.contains(form.getPlatform())) {
                marketOrdersData.setForm(form);
                str = (str.replaceAll(form.getPlatform(), ""));
                break;
            }
        }

        // 关键字
        marketOrdersData.setKey(str.replaceAll(CommandConstants.WARFRAME_MARKET_ORDERS_CMD, "").trim());
        if (marketOrdersData.getKey().isEmpty()) {
            bot.sendMsg(event, "请输入正确的名称！", false);
            return;
        }
        try {
            log.debug("用户:{} 指令 {} 执行参数 {}", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD, objectMapper.writeValueAsString(marketOrdersData));
        } catch (Exception e) {
            log.debug("序列化参数失败: {}", e.getMessage());
        }

        try {
            byte[] bytes = postMarketOrdersImage(marketOrdersData);
            SendUtils.send(bot, event, bytes, Codes.WARFRAME_MARKET_ORDERS_PLUGIN, log);
        } catch (NullPointerException e) {
            bot.sendMsg(event, "查询失败！请检查名称是否正确。", false);
        }

    }

    private byte[] postMarketOrdersImage(MarketOrdersData data) {
        MarketUtils.Market market = MarketUtils.toSet(data.getKey(), data.getForm());
        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            return drawImagePlugin.drawMarketOrdersImage(market.getPossibleItems());
        }
        BaseOrder<OrderWithUser> order = MarketUtils.market(data.getForm(), data.isBy(), data.isMax(), market);
        Orders orders = getOrders(data, order, market);
        return drawImagePlugin.drawMarketOrdersImage(orders);
    }

    @Getter
    @Setter
    private static class MarketOrdersData {
        private MarketPlatformEnum form = MarketPlatformEnum.PC;
        private boolean isBy;
        private boolean isMax;
        private String key;
    }
}

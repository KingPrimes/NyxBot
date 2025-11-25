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
import com.nyx.bot.modules.warframe.utils.MarketOrderUtils;
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

    private static Orders getOrders(MarketOrdersData data, BaseOrder<OrderWithUser> order, MarketOrderUtils.Market market) {
        List<OrderWithUser> ows = order.getData();
        OrdersItems oi = market.getItem();
        return new Orders().setName(oi.getName())
                .setForm(data.getForm())
                .setIsBy(data.isBy)
                .setIsMax(data.isMax)
                .setDucats(defaultIfNull(oi.getDucats()))
                .setVaulted(oi.getVaulted())
                .setMaxAmberStars(defaultIfNull(oi.getMaxAmberStars()))
                .setMaxCyanStars(defaultIfNull(oi.getMaxCyanStars()))
                .setBaseEndo(defaultIfNull(oi.getBaseEndo()))
                .setReqMasteryRank(defaultIfNull(oi.getReqMasteryRank()))
                .setTradingTax(defaultIfNull(oi.getTradingTax()))
                .setIcon(null)
                .setOrders(ows);
    }

    private static Integer defaultIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(startWith = {"/WM", "WM", "/市场", "市场", "/wm", "wm"}, at = AtEnum.BOTH)
    public void marketOrders(Bot bot, AnyMessageEvent event) {
        try {
            // 1. 预处理输入消息
            String processedInput = preprocessInput(event);

            // 2. 创建数据容器
            MarketOrdersData marketOrdersData = new MarketOrdersData();

            // 3. 处理特殊指令
            String normalizedInput = handleSpecialCommands(processedInput, event);

            // 4. 处理参数
            processMarketParameters(normalizedInput, marketOrdersData);

            // 5. 验证并发送结果
            executeMarketQuery(bot, event, marketOrdersData);
        } catch (Exception e) {
            handleMarketOrdersError(bot, event, e);
        }
    }

    /**
     * 预处理用户输入
     */
    private String preprocessInput(AnyMessageEvent event) {
        String str = event.getMessage().trim();
        str = ShiroUtils.unescape(str).toUpperCase();
        return str;
    }

    /**
     * 处理特殊符号和无效指令
     *
     * @return 处理后的字符串，如果指令无效返回""
     */
    private String handleSpecialCommands(String str, AnyMessageEvent event) {
        if (MatcherUtils.isSpecialSymbols(str)) {
            String orderItem = MatcherUtils.isOrderItem(str);
            if (orderItem.isEmpty()) {
                log.debug("用户:{} 输入了错误的指令:{}", event.getUserId(), str);
                return "";
            }
            return orderItem;
        }
        return str;
    }

    /**
     * 处理市场参数（满级、买卖、平台等）
     */
    private void processMarketParameters(String input, MarketOrdersData data) {
        String processed = processMaxLevelParam(input, data);
        processed = processBuySellParam(processed, data);
        processed = processPlatformParam(processed, data);

        // 提取最终搜索关键字
        String key = processed.replaceAll(CommandConstants.WARFRAME_MARKET_ORDERS_CMD, "").trim();
        data.setKey(key);
    }

    /**
     * 处理满级参数
     */
    private String processMaxLevelParam(String input, MarketOrdersData data) {
        if (input.contains("满级") || input.contains("MAX")) {
            String result = input.replaceAll("满级", "").replaceAll("MAX", "");
            data.setMax(true);
            return result;
        } else {
            data.setMax(false);
        }
        return input;
    }

    /**
     * 处理买卖参数
     */
    private String processBuySellParam(String input, MarketOrdersData data) {
        String processed = input;

        // 处理购买相关关键字
        if (input.contains("购买") || input.contains("买家") || input.contains("BUY")) {
            processed = input.replaceAll("购买", "").replaceAll("买家", "").replaceAll("BUY", "");
            data.setBy(true);
        }

        // 处理出售相关关键字
        if (input.contains("出售") || input.contains("卖家") || input.contains("SELL")) {
            processed = input.replaceAll("出售", "").replaceAll("卖家", "").replaceAll("SELL", "");
            data.setBy(false);
        }

        return processed;
    }

    /**
     * 处理平台参数
     */
    private String processPlatformParam(String input, MarketOrdersData data) {
        String processed = input;
        for (MarketPlatformEnum platform : MarketPlatformEnum.values()) {
            if (processed.contains(platform.getPlatform())) {
                data.setForm(platform);
                processed = processed.replaceAll(platform.getPlatform(), "");
                break;
            }
        }
        return processed;
    }

    /**
     * 执行市场查询并发送结果
     */
    private void executeMarketQuery(Bot bot, AnyMessageEvent event, MarketOrdersData data) {
        // 验证搜索关键词
        if (data.getKey().isEmpty()) {
            bot.sendMsg(event, "请输入正确的名称！", false);
            return;
        }

        // 记录执行参数
        logCommandExecution(event, data);

        try {
            byte[] imageBytes = postMarketOrdersImage(data);
            SendUtils.send(bot, event, imageBytes, Codes.WARFRAME_MARKET_ORDERS_PLUGIN, log);
        } catch (Exception e) {
            handleMarketOrdersError(bot, event, e);
        }
    }

    /**
     * 记录命令执行日志
     */
    private void logCommandExecution(AnyMessageEvent event, MarketOrdersData data) {
        try {
            log.debug("用户:{} 指令 {} 执行参数 {}",
                    event.getUserId(),
                    CommandConstants.WARFRAME_MARKET_ORDERS_CMD,
                    objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            log.debug("序列化参数失败: {}", e.getMessage());
        }
    }

    /**
     * 处理市场订单错误
     */
    private void handleMarketOrdersError(Bot bot, AnyMessageEvent event, Exception e) {
        log.error("处理市场订单时发生错误", e);

        String errorMessage = "查询失败！请检查名称是否正确。";
        if (e instanceof NullPointerException) {
            errorMessage = "查询失败！请检查名称是否正确。";
        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
            errorMessage = "查询超时，请稍后再试。";
        }

        bot.sendMsg(event, errorMessage, false);
    }

    /**
     * 根据市场订单数据生成对应的图像
     *
     * @param data 市场订单数据，包含查询关键字和表单信息
     * @return 生成的市场订单图像字节数组
     */
    private byte[] postMarketOrdersImage(MarketOrdersData data) {
        MarketOrderUtils.Market market = MarketOrderUtils.toSet(data.getKey(), data.getForm());
        // 如果存在可能的物品列表，则直接绘制这些物品的图像
        if (market.getPossibleItems() != null && !market.getPossibleItems().isEmpty()) {
            return drawImagePlugin.drawMarketOrdersImage(market.getPossibleItems());
        }
        BaseOrder<OrderWithUser> order = MarketOrderUtils.market(data.getForm(), data.isBy(), data.isMax(), market);
        Orders orders = getOrders(data, order, market);
        // 绘制订单图像
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

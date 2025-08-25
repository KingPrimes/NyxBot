package com.nyx.bot.modules.warframe.plugin;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询 Market 市场订单
 */
@Shiro
@Component
@Slf4j
public class MarketOrdersPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(startWith = {"/WM", "WM", "/市场", "市场", "/wm", "wm"})
    public void marketOrders(Bot bot, AnyMessageEvent event) {
        // TODO 查询市场更新到V2接口，目前暂不可用，加急任务

        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD);
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

        OneBotLogInfoData data = WarframeSend.getLogInfoData(bot, event, Codes.WARFRAME_MARKET_ORDERS_PLUGIN);

        if (str.contains("满级") || str.contains("MAX")) {
            str = (str.replaceAll("满级", "").replaceAll("MAX", ""));
            data.setIsMax(true);
        }
        if (str.contains("购买") || str.contains("买家") || str.contains("BUY")) {
            str = (str.replaceAll("购买", "").replaceAll("买家", "").replaceAll("BUY", ""));
            data.setIsBy(true);
        }

        if (str.contains("出售") || str.contains("卖家") || str.contains("SELL")) {
            str = (str.replaceAll("出售", "").replaceAll("卖家", "").replaceAll("SELL", ""));
            data.setIsBy(false);
        }
        // 获取平台
        String finalStr = str;
        for (MarketFormEnums form : MarketFormEnums.values()) {
            if (finalStr.contains(form.getForm())) {
                data.setForm(form);
                str = (str.replaceAll(form.getForm(), ""));
                break;
            }
        }

        // 关键字
        data.setKey(str.replaceAll(CommandConstants.WARFRAME_MARKET_ORDERS_CMD, "").trim());
        if (data.getKey().isEmpty()) {
            bot.sendMsg(event, "请输入正确的名称！", false);
            return;
        }
        log.debug("用户:{} 指令 {} 执行参数 {}", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD, JSON.toJSONString(data));
        // 发送POST请求获取生成得图片
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postMarketOrdersImage",
                data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("用户:{} 指令 {} 执行成功", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD);
        } else {
            WarframeSend.sendErrorMsg(bot, event, body);
            log.debug("用户:{} 指令 {} 执行失败", event.getUserId(), CommandConstants.WARFRAME_MARKET_ORDERS_CMD);
        }
    }
}

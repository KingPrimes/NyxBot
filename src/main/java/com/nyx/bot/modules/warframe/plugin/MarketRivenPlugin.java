package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询 Market 市场拍卖的紫卡
 */
@Shiro
@Component
@Slf4j
public class MarketRivenPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_RIVEN_CMD)
    public void marketRiven(Bot bot, AnyMessageEvent event) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.WARFRAME_MARKET_RIVEN_CMD);
        String str = event.getMessage();
        if (MatcherUtils.isSpecialSymbols(str)) {
            String item = MatcherUtils.isOrderItem(str);
            if (item.isEmpty()) {
                log.debug("用户:{} 输入了错误的指令 {}", event.getUserId(), str);
                bot.sendMsg(event, "请输入正确的指令！", false);
                return;
            }
            str = item;
        }
        OneBotLogInfoData data = WarframeSend.getLogInfoData(bot, event, Codes.WARFRAME_MARKET_RIVEN_PLUGIN);
        data.setKey(str.replaceAll(data.getCodes().getComm(), "").trim());
        log.debug("用户:{} 指令 {} 执行开始", event.getUserId(), data.getCodes().getComm());
        log.debug("用户:{} 指令 {} 执行参数 {}", event.getUserId(), data.getCodes().getComm(), data.getKey());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postMarketRivenImage",
                data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("用户:{} 指令 {} 执行成功", event.getUserId(), data.getCodes().getComm());
        } else {
            WarframeSend.sendErrorMsg(bot, event, body);
            log.debug("用户:{} 指令 {} 执行失败", event.getUserId(), data.getCodes().getComm());
        }
    }
}

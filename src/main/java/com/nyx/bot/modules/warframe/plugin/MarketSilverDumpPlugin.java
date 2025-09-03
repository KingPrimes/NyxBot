package com.nyx.bot.modules.warframe.plugin;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.res.Ducats;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 查询 Market市场中的银垃圾
 */
@Shiro
@Component
@Slf4j
public class MarketSilverDumpPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_SILVER_DUMP_CMD)
    public void marketSilverDumpHandler(Bot bot, AnyMessageEvent event) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.WARFRAME_MARKET_SILVER_DUMP_CMD);
        OneBotLogInfoData data = WarframeSend.getLogInfoData(bot, event, Codes.WARFRAME_MARKET_SILVER_DUMP);
        Ducats ducats = MarketUtils.getDucats();
        if (Objects.isNull(ducats)) {
            log.debug("获取ducat失败");
            bot.sendMsg(event, "获取ducat失败", false);
            return;
        }
        data.setData(JSON.toJSONString(Objects.requireNonNull(ducats).getPayload().getGodDump()));
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postMarketDucatsImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    ArrayMsgUtils.builder().img(body.getFile()).build(), false);
        } else {
            WarframeSend.sendErrorMsg(bot, event, body);
        }
    }
}

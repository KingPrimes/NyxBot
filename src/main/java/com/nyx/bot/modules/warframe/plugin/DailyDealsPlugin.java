package com.nyx.bot.modules.warframe.plugin;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 每日特惠
 */
@Shiro
@Component
@Slf4j
public class DailyDealsPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_DAILY_DEALS_CMD)
    public void dailyDealsHandler(Bot bot, AnyMessageEvent event) {
        WarframeSend.send(bot, event, "postDailyDealsImage", Codes.WARFRAME_DAILY_DEALS_PLUGIN, log);
    }
}

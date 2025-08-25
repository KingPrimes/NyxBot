package com.nyx.bot.modules.warframe.plugin;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 遗物查询
 */
@Shiro
@Component
@Slf4j
public class RelicsPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RELICS_CMD)
    public void relics(Bot bot, AnyMessageEvent event) {
        OneBotLogInfoData data = WarframeSend.getLogInfoData(bot, event, Codes.WARFRAME_RELICS_PLUGIN);
        data.setData(event.getRawMessage().replaceAll(CommandConstants.WARFRAME_RELICS_CMD,"").trim());
        WarframeSend.sendForData(bot, event, "postRelicsImage", data, log);
    }
}

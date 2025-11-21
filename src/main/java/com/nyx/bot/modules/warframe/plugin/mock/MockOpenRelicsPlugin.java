package com.nyx.bot.modules.warframe.plugin.mock;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.onebot.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模拟开核桃
 */
@Shiro
@Component
@Slf4j
public class MockOpenRelicsPlugin {

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_OPEN_RELICS_CMD)
    public void openRelics(Bot bot, AnyMessageEvent event) {
        // TODO 实现模拟开核桃
        SendUtils.not(bot, event);
    }
}

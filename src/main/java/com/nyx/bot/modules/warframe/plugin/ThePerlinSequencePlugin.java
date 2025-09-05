package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 查询 佩兰数列 武器轮换信息
 */
@Shiro
@Component
@Slf4j
public class ThePerlinSequencePlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_THE_PERLIN_SEQUENCE_CMD)
    public void thePerlinSequence(Bot bot, AnyMessageEvent event) {
        // TODO 实现查询 佩兰数列 武器轮换信息
        SendUtils.not(bot, event);
    }
}

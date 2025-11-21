package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.onebot.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 翻译
 */
@Shiro
@Component
@Slf4j
public class TranslationPlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_TRA_CMD,at = AtEnum.BOTH)
    public void translation(Bot bot, AnyMessageEvent event) {
        //TODO 实现翻译数据
        SendUtils.not(bot, event);
    }
}

package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.onebot.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 武器倾向变动
 */
@Shiro
@Component
@Slf4j
public class RivenDisUpdatePlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_DIS_UPDATE_CMD, at = AtEnum.BOTH)
    public void rivenDisUpdate(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        // TODO 实现查询 武器倾向变动
        SendUtils.not(bot, event);
    }
}

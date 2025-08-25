package com.nyx.bot.modules.system.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Shiro
@Component
@Slf4j
public class SystemInfoPlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.CHECK_VERSION_CMD)
    public void systemInfoHandler(Bot bot, AnyMessageEvent event) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), CommandConstants.CHECK_VERSION_CMD);
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "systemInfo",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("群：{} 用户:{} 指令 {} 执行成功", event.getGroupId(), event.getUserId(), CommandConstants.CHECK_VERSION_CMD);
        } else {
            log.debug("群：{} 用户:{} 指令 {} 执行失败", event.getGroupId(), event.getUserId(), CommandConstants.CHECK_VERSION_CMD);
            bot.sendMsg(event, body.getBody(), false);
        }

    }

}

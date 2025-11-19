package com.nyx.bot.modules.help.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import io.github.kingprimes.DrawImagePlugin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Shiro
@Component
@Slf4j
public class HelpPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.HELP_CMD, at = AtEnum.BOTH)
    public void helpHandler(Bot bot, AnyMessageEvent event) {
        SendUtils.send(bot, event, getHelpImage(), Codes.HELP, log);
        bot.sendMsg(event, ArrayMsgUtils.builder()
                .text("指令使用方法请查看以下文档：https://kingprimes.top/posts/1bb16eb")
                .build(), false);

    }

    private byte[] getHelpImage() {
        List<String> collect = Arrays.stream(Codes.values()).map(c -> StringUtils.removeMatcher(c.getComm())).collect(Collectors.toList());
        return drawImagePlugin.drawHelpImage(collect);
    }
}

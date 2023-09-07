package com.nyx.bot.plugin.help;


import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.utils.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;


public class HelpCode {

    //菜单，列出所有的指令与权限
    public static void help(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body bytes = ImageUrlUtils.builderBase64Post(
                "help",
                bot, event);
        if (bytes.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(bytes.getFile()).build(), false);
        }

    }
}

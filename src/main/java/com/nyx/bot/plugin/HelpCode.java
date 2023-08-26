package com.nyx.bot.plugin;


import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;


public class HelpCode {

    //菜单，列出所有的指令与权限
    public static void help(Bot bot, AnyMessageEvent event) {
        byte[] bytes = ImageUrlUtils.builderBase64Post(
                "help",
                bot, event);
        if (bytes != null) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(bytes).build(), false);
        }

    }
}

package com.nyx.bot.plugin;


import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.utils.onebot.ImageUrlUtils;


public class HelpCode {

    //菜单，列出所有的指令与权限
    public static void help(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event,
                MsgUtils.builder()
                        .img(
                                ImageUrlUtils.builder(
                                        "help",
                                        bot, event)
                        ).build(), false);
    }
}

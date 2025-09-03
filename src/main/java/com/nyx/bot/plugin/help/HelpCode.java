package com.nyx.bot.plugin.help;


import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;


public class HelpCode {

    //菜单，列出所有的指令与权限
    public static void help(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body bytes = ImageUrlUtils.builderBase64Post(
                "help",
                bot, event);
        bot.sendMsg(event,
                ArrayMsgUtils.builder().img(bytes.getFile()).build(), false);
        bot.sendMsg(event, ArrayMsgUtils.builder()
                .text("指令使用方法请查看以下文档：https://kingprimes.top/posts/1bb16eb")
                .build(), false);


    }
}

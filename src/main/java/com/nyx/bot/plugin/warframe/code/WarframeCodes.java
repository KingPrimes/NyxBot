package com.nyx.bot.plugin.warframe.code;

import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;

public class WarframeCodes {


    /**
     * 订阅消息
     * @param bot Bot
     * @param event 消息体
     * @param code 指令
     */
    public static void subscribe(Bot bot, AnyMessageEvent event, String code){
        if(!ActionParams.GROUP.equals(event.getMessageType())){
            bot.sendMsg(event,"改指令只能在群组中使用！",false);
            return;
        }

        if(code.isEmpty()){
            bot.sendMsg(event, "请在订阅指令后面加上要订阅的编号！\n编号通过使用 订阅列表查看！",false);
        }

        bot.sendMsg(event,"订阅成功！",false);
    }
}

package com.nyx.bot.utils;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.enums.Codes;
import org.slf4j.Logger;

public class SendUtils {
    public static void send(Bot bot, AnyMessageEvent event, byte[] imageBytes, Codes code, Logger log) {
        if (imageBytes != null && imageBytes.length > 0) {
            // 直接使用字节数组构建图片消息
            bot.sendMsg(event, ArrayMsgUtils.builder().img(imageBytes).build(), false);
            log.debug("群：{} 用户:{} 指令 {} 执行成功", event.getGroupId(), event.getUserId(), code.getComm());
            return;
        }
        sendErrorMsg(bot, event, code);
        log.debug("群：{} 用户:{} 指令 {} 执行失败", event.getGroupId(), event.getUserId(), code.getComm());
    }

    public static void sendErrorMsg(Bot bot, AnyMessageEvent event, Codes codes) {
        bot.sendMsg(event, codes.getComm() + " 执行失败", false);
    }

    public static void not(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "该功能暂未实现！", false);
    }
}

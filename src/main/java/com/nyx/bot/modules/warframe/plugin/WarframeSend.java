package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import org.slf4j.Logger;

public class WarframeSend {
    public static void send(Bot bot, AnyMessageEvent event, String url, Codes code, Logger log) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), code.getComm());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                url,
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("群：{} 用户:{} 指令 {} 执行成功", event.getGroupId(), event.getUserId(), code.getComm());
            return;
        }
        sendErrorMsg(bot, event, body);
        log.debug("群：{} 用户:{} 指令 {} 执行失败", event.getGroupId(), event.getUserId(), code.getComm());
    }

    public static void sendForCode(Bot bot, AnyMessageEvent event, String url, Codes code, Logger log) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), code.getComm());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                url,
                getLogInfoData(bot, event, code));
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("群：{} 用户:{} 指令 {} 执行成功", event.getGroupId(), event.getUserId(), code.getComm());
        } else {
            sendErrorMsg(bot, event, body);
            log.debug("群：{} 用户:{} 指令 {} 执行失败", event.getGroupId(), event.getUserId(), code.getComm());
        }
    }

    public static void sendForData(Bot bot, AnyMessageEvent event, String url, OneBotLogInfoData data, Logger log) {
        log.debug("群：{} 用户:{} 使用了 {} 指令", event.getGroupId(), event.getUserId(), data.getCodes().getComm());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                url,
                data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            log.debug("群：{} 用户:{} 指令 {} 执行成功", event.getGroupId(), event.getUserId(), data.getCodes().getComm());
        } else {
            sendErrorMsg(bot, event, body);
            log.debug("群：{} 用户:{} 指令 {} 执行失败", event.getGroupId(), event.getUserId(), data.getCodes().getComm());
        }
    }

    public static void sendErrorMsg(Bot bot, AnyMessageEvent event, HttpUtils.Body body) {
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event, body.getBody(), false);
        }
    }

    public static OneBotLogInfoData getLogInfoData(Bot bot, AnyMessageEvent event, Codes code) {
        OneBotLogInfoData oneBotLogInfoData = new OneBotLogInfoData();
        oneBotLogInfoData.setCodes(code);
        oneBotLogInfoData.setBotUid(bot.getSelfId());
        oneBotLogInfoData.setUserUid(event.getUserId());
        oneBotLogInfoData.setGroupUid(event.getGroupId());
        oneBotLogInfoData.setRawMsg(event.getRawMessage());
        oneBotLogInfoData.setTime(DateUtils.getDate());
        oneBotLogInfoData.setPermissionsEnums(Permissions.checkAdmin(bot, event));
        return oneBotLogInfoData;
    }

    public static void not(Bot bot, AnyMessageEvent event) {
        bot.sendMsg(event, "该功能暂未实现！", false);
    }

}

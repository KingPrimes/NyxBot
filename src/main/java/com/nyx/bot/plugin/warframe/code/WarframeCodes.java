package com.nyx.bot.plugin.warframe.code;

import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;

public class WarframeCodes {


    /**
     * 订阅消息
     * @param bot Bot
     * @param event 消息体
     * @param code 指令
     */
    public static void subscribe(Bot bot, AnyMessageEvent event, String code){
        if(!ActionParams.GROUP.equals(event.getMessageType())){
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }

        if(code.isEmpty()){
            bot.sendMsg(event, "请在订阅指令后面加上要订阅的编号！\n编号通过使用 订阅列表查看！",false);
        }

        bot.sendMsg(event,"订阅成功！",false);
    }

    /**
     * 平原
     */
    public static void allCycle(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getAllCycleImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 仲裁
     */
    public static void arbitration(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getArbitrationImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 执政官突击
     */
    public static void arsonHun(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getArsonHuntImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 突击
     */
    public static void assault(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getAssaultImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 每日特惠
     */
    public static void dailyDeals(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getDailyDealsImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 裂隙
     */
    public static void fissues(Bot bot, AnyMessageEvent event, Codes code) {
        OneBotLogInfoData oneBotLogInfoData = new OneBotLogInfoData();
        oneBotLogInfoData.setCodes(code);
        oneBotLogInfoData.setBotUid(bot.getSelfId());
        oneBotLogInfoData.setUserUid(event.getUserId());
        oneBotLogInfoData.setGroupUid(event.getGroupId());
        oneBotLogInfoData.setRawMsg(event.getRawMessage());
        oneBotLogInfoData.setTime(DateUtils.getDate());
        oneBotLogInfoData.setPermissionsEnums(Permissions.checkAdmin(bot, event));

        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getFissuesImage",
                oneBotLogInfoData);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            bot.sendMsg(event, "裂隙获取失败", false);
        }
    }

    /**
     * 入侵
     */
    public static void invasions(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getInvasionsImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 电波
     */
    public static void nighTwave(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getNighTwaveImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 钢铁
     */
    public static void steelPath(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getSteelPathImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

    /**
     * 奸商
     */
    public static void aVoid(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getVoidImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
    }

}

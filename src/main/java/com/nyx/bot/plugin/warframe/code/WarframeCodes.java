package com.nyx.bot.plugin.warframe.code;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.plugin.warframe.utils.RivenAttributeCompute;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WarframeCodes {

    private static OneBotLogInfoData getLogInfoData(Bot bot, AnyMessageEvent event, Codes code) {
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

    private static void sendErrorMsg(Bot bot, AnyMessageEvent event, HttpUtils.Body body) {
        if (!body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event, body.getBody(), false);
        }
    }


    /**
     * 订阅消息
     *
     * @param bot   Bot
     * @param event 消息体
     */
    public static void subscribe(Bot bot, AnyMessageEvent event) {
        if (!ActionParams.GROUP.equals(event.getMessageType())) {
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }
        String code = event.getRawMessage().replaceAll(Codes.WARFRAME_SUBSCRIBE.getStr(), "").trim();
        if (code.isEmpty()) {
            bot.sendMsg(event, "请在订阅指令后面加上要订阅的编号！\n编号通过使用 订阅列表查看！", false);
        }

        bot.sendMsg(event, "订阅成功！", false);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
    }

    /**
     * 裂隙
     */
    public static void fissues(Bot bot, AnyMessageEvent event, Codes code) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getFissuesImage",
                getLogInfoData(bot, event, code));
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            bot.sendMsg(event, "裂隙获取失败", false);
        }
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
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
        sendErrorMsg(bot, event, body);
    }

    /**
     * Market Orders 查询
     */
    public static void orders(Bot bot, AnyMessageEvent event, String str, Codes code) {
        str = ShiroUtils.unescape(str).toUpperCase();

        if (MatcherUtils.isSpecialSymbols(str)) {
            String orderItem = MatcherUtils.isOrderItem(str);
            if (orderItem.isEmpty()) {
                bot.sendMsg(event, "请输入正确的指令！", false);
                return;
            }
            str = orderItem;
        }

        OneBotLogInfoData data = getLogInfoData(bot, event, code);

        //是否是满级
        data.setIsMax(false);
        //是否是购买
        data.setIsBy(false);
        //是否是卖家
        data.setIsBy(false);
        //平台
        data.setForm(MarketFormEnums.PC);

        if (str.contains("满级") || str.contains("MAX")) {
            str = (str.replaceAll("满级", "").replaceAll("MAX", ""));
            data.setIsMax(true);
        }
        if (str.contains("购买") || str.contains("买家") || str.contains("BUY")) {
            str = (str.replaceAll("购买", "").replaceAll("买家", "").replaceAll("BUY", ""));
            data.setIsBy(true);
        }

        if (str.contains("出售") || str.contains("卖家") || str.contains("SELL")) {
            str = (str.replaceAll("出售", "").replaceAll("卖家", "").replaceAll("SELL", ""));
            data.setIsBy(false);
        }
        //获取平台
        String finalStr = str;
        for (MarketFormEnums form : MarketFormEnums.values()) {
            if (finalStr.contains(form.getForm())) {
                data.setForm(form);
                str = (str.replaceAll(form.getForm(), ""));
                break;
            }
        }

        //关键字
        data.setKey(str.replaceAll(code.getStr(), "").trim());

        //发送POST请求获取生成得图片
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postMarketOrdersImage",
                data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }
    }

    /**
     * Warframe Market Riven拍卖查询
     */
    public static void marketRiven(Bot bot, AnyMessageEvent event, String str, Codes code) {

    }

    /**
     * 紫卡属性计算
     */
    public static void ocrRivenCompute(Bot bot, AnyMessageEvent event, Codes code) {
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        if (msgImgUrlList.isEmpty()) {
            bot.sendMsg(event, "请在指令后方添加上您要查询的紫卡图片!", false);
            return;
        }
        if (msgImgUrlList.size() > 5) {
            bot.sendMsg(event, "查询紫卡图片一次性不可大于5张", false);
            return;
        }
        OneBotLogInfoData data = getLogInfoData(bot, event, code);
        data.setData(RivenAttributeCompute.ocrRivenCompute(event));
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postRivenAnalyseImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }
    }

    public static void ducat(Bot bot, AnyMessageEvent event) {

    }

}

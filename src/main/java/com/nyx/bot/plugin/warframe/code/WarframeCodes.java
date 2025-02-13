package com.nyx.bot.plugin.warframe.code;

import com.alibaba.fastjson2.JSON;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.MarketFormEnums;
import com.nyx.bot.permissions.Permissions;
import com.nyx.bot.plugin.warframe.utils.MarketUtils;
import com.nyx.bot.plugin.warframe.utils.RivenAttributeCompute;
import com.nyx.bot.plugin.warframe.utils.WarframeSubscribeCheck;
import com.nyx.bot.res.Ducats;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.MatcherUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

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
     * 警报
     */
    public static void alerts(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getAlertsImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
        sendErrorMsg(bot, event, body);
    }


    /**
     * 平原
     */
    public static void allCycle(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postAllCycleImage",
                bot, event);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        }
        sendErrorMsg(bot, event, body);
    }

    public static void duviriCycle(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postDuviriCycleImage",
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
     * 仲裁Ex
     */
    public static void arbitrationEx(Bot bot, AnyMessageEvent event) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "getArbitrationExImage",
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
                "postArsonHuntImage",
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
                "postAssaultImage",
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
                "postDailyDealsImage",
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
    public static void fissures(Bot bot, AnyMessageEvent event, Codes code) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postFissuresImage",
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
                "postInvasionsImage",
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
                "postNighTwaveImage",
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
                "postSteelPathImage",
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
                "postVoidImage",
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
        //是否是买家
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
        if (MatcherUtils.isSpecialSymbols(str)) {
            String item = MatcherUtils.isOrderItem(str);
            if (item.isEmpty()) {
                bot.sendMsg(event, "请输入正确的指令！", false);
                return;
            }
            str = item;
        }
        OneBotLogInfoData data = getLogInfoData(bot, event, code);
        data.setKey(str.replaceAll(code.getStr(), "").trim());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                "postMarketRivenImage",
                data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }
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

    /**
     * 金银垃圾
     */
    public static void ducat(Bot bot, AnyMessageEvent event, Codes code) {
        OneBotLogInfoData data = getLogInfoData(bot, event, code);
        Ducats ducats = MarketUtils.getDucats();
        if (Objects.isNull(ducats)) {
            bot.sendMsg(event, "获取ducat失败", false);
        }
        switch (code) {
            case WARFRAME_MARKET_GOD_DUMP ->
                    data.setData(JSON.toJSONString(Objects.requireNonNull(ducats).getPayload().getGodDump()));
            case WARFRAME_MARKET_SILVER_DUMP ->
                    data.setData(JSON.toJSONString(Objects.requireNonNull(ducats).getPayload().getSilverDump()));
        }
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postMarketDucatsImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }
    }

    /**
     * 紫卡倾向列表
     */
    public static void rivenDisUpdate(Bot bot, AnyMessageEvent event, Codes code) {
        OneBotLogInfoData data = getLogInfoData(bot, event, code);
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postRivenDispositionUpdatesImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }

    }

    /**
     * 订阅消息
     *
     * @param bot   Bot
     * @param event 消息体
     */
    public static void subscribe(Bot bot, AnyMessageEvent event, Codes code) {
        if (!ActionParams.GROUP.equals(event.getMessageType())) {
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }
        String str = event.getRawMessage().replace("订阅", "").trim();

        if (str.isEmpty()) {
            OneBotLogInfoData data = getLogInfoData(bot, event, code);
            HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postsubscribeHelp", data);
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            return;
        }

        String ms = WarframeSubscribeCheck.userSubscriptions(bot.getSelfId(),
                event.getUserId(),
                bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), false).getData().getNickname(),
                event.getGroupId(),
                bot.getGroupInfo(event.getGroupId(), false).getData().getGroupName(),
                str
        );

        bot.sendMsg(event, ms, false);
    }

    /**
     * 取消订阅消息
     */
    public static void cancelSubscribe(Bot bot, AnyMessageEvent event, Codes code) {
        if (!ActionParams.GROUP.equals(event.getMessageType())) {
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }
        String str = event.getRawMessage().replace("取消订阅", "").trim();

        if (str.isEmpty()) {
            OneBotLogInfoData data = getLogInfoData(bot, event, code);
            HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postsubscribeHelp", data);
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
            return;
        }

        String ms = WarframeSubscribeCheck.userCancelSubscribe(
                event.getUserId(),
                event.getGroupId(),
                str
        );
        bot.sendMsg(event, ms, false);
    }


    public static void relics(Bot bot, AnyMessageEvent event) {
        OneBotLogInfoData data = getLogInfoData(bot, event, Codes.WARFRAME_RELICS_PLUGIN);
        data.setData(event.getRawMessage().replaceAll(Codes.WARFRAME_RELICS_PLUGIN.getStr(), "").trim());
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postRelicsImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            sendErrorMsg(bot, event, body);
        }
    }
}

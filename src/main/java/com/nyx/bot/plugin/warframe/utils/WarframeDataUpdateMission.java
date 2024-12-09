package com.nyx.bot.plugin.warframe.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.core.OneBotLogInfoData;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.entity.warframe.MissionSubscribeUserCheckType;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.enums.PermissionsEnums;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class WarframeDataUpdateMission {


    static MissionSubscribeRepository repository = SpringUtils.getBean(MissionSubscribeRepository.class);


    /**
     * 警报更新提醒
     */
    public static void updateAlerts() {
        sendGroupsToUser(SubscribeEnums.ALERTS, I18nUtils.message("warframe.up.alerts"));
    }

    /**
     * 仲裁更新提醒
     */
    public static void updateArbitration() {
        sendGroupsToUser(SubscribeEnums.ARBITRATION, I18nUtils.message("warframe.up.arbitration"));
    }

    /**
     * 每日特惠更新 提醒
     */
    public static void updateDailyDeals() {
        sendGroupsToUser(SubscribeEnums.DAILY_DEALS, I18nUtils.message("warframe.up.dayDeals"));
    }

    /**
     * 活动更新提醒
     */
    public static void updateEvents() {
        sendGroupsToUser(SubscribeEnums.EVENTS, I18nUtils.message("warframe.up.newEvents"));
    }

    /**
     * 裂隙
     */
    public static void updateFissures() {
        sendGroupsToUser(SubscribeEnums.FISSURES, I18nUtils.message("warframe.up.newFissures"));
    }

    /**
     * 新的入侵
     */
    public static void updateInvasions() {
        sendGroupsToUser(SubscribeEnums.INVASIONS, I18nUtils.message("warframe.up.invasions"));
    }

    /**
     * 新闻
     */
    public static void updateNews() {
    }

    /**
     * 钢铁之路兑换轮换
     */
    public static void updateSteelPath() {
        sendGroupsToUser(SubscribeEnums.STEEL_PATH, I18nUtils.message("warframe.up.steelPath"));
    }

    /**
     * 虚空商人 离开/到来 提醒
     *
     * @param msg 骚话！
     */
    public static void updateVoidTrader(String msg) {
        sendGroupsToUser(SubscribeEnums.VOID, msg);
    }

    /**
     * 到黑夜前提醒
     */
    public static void updateCetusCycle(String time) {
        sendGroupsToUser(SubscribeEnums.CETUS_CYCLE, I18nUtils.message("warframe.up.cetusCycle") + time);
    }

    /**
     * 电波
     */
    public static void updateNightwave() {
    }

    /**
     * 突击
     */
    public static void updateSortie() {
    }

    /**
     * 执政官突击
     */
    public static void updateArchonHunt() {
    }

    /**
     * 双衍王境
     */
    public static void updateDuviriCycle() {
    }

    static String gestural(SubscribeEnums enums) {
        switch (enums) {
            case ARBITRATION -> {
                return "getArbitrationImage";
            }
            case DAILY_DEALS -> {
                return "getDailyDealsImage";
            }
            case VOID -> {
                return "getVoidImage";
            }
            case CETUS_CYCLE -> {
                return "getAllCycleImage";
            }
            case INVASIONS -> {
                return "getInvasionsImage";
            }
            case STEEL_PATH -> {
                return "getSteelPathImage";
            }
            case NIGHTWAVE -> {
                return "getNighTwaveImage";
            }
            case SORTIE -> {
                return "getAssaultImage";
            }
            default -> {
                return "";
            }
        }
    }

    /**
     * 通知所有的订阅群组与用户
     *
     * @param enums   通知类型
     * @param msgText 文本消息
     */
    private static void sendGroupsToUser(SubscribeEnums enums, String msgText) {
        //获取所有订阅
        List<MissionSubscribe> subscribes = repository.findAll();
        if (subscribes.isEmpty()) {
            return;
        }
        //获取Bots
        Map<Long, Bot> bots = SpringUtils.getBean(BotContainer.class).robots;

        if (bots.isEmpty()) {
            log.warn("未链接Bot无法发送订阅消息");
            return;
        }

        try {
            //遍历所有的订阅
            for (MissionSubscribe subscribe : subscribes) {
                //构建消息体
                Msg msg = new Msg();
                //设置消息
                if (!Objects.equals(msgText, "")) {
                    msg.text(msgText + "\n");
                }
                List<Long> botList = bots.keySet().stream().filter(bot -> subscribe.getSubBotUid().equals(bot)).toList();
                for (Long l : botList) {
                    if (!subscribe.getSubBotUid().equals(l)) {
                        continue;
                    }
                    Long subGroup = subscribe.getSubGroup();
                    //获取订阅用户
                    List<MissionSubscribeUser> subUsers = subscribe.getSubUsers().stream()
                            .filter(u ->
                                    u.getTypeList().stream()
                                            .filter(t -> t.getSubscribe().equals(enums))
                                            .count() > 1).toList();
                    for (MissionSubscribeUser user : subUsers) {
                        //获取是否是裂隙类型的订阅
                        for (MissionSubscribeUserCheckType userCheckType : user.getTypeList()) {
                            switch (userCheckType.getSubscribe()) {
                                case FISSURES -> {

                                }
                                case INVASIONS -> {

                                }
                                case ARBITRATION -> {

                                }
                                case VOID, ALERTS, CETUS_CYCLE, DAILY_DEALS, STEEL_PATH, NEWS, NIGHTWAVE ->
                                        ConstructTheReturnInformation(msg, enums, l, user.getUserId(), subGroup);

                            }
                        }
                        bots.get(l).sendGroupMsg(subGroup, msg.build(), false);
                    }

                }
            }
        } catch (Exception e) {
            log.error("发送订阅消息失败：{}", e.getMessage());
        }

    }

    static void ConstructTheReturnInformation(Msg msg, SubscribeEnums enums, Long bot, Long user, Long group) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                gestural(enums), new OneBotLogInfoData(
                        bot,
                        user,
                        group,
                        "",
                        "",
                        PermissionsEnums.MANAGE,
                        Codes.WARFRAME_SUBSCRIBE,
                        "")
        );
        msg.at(user).text("您订阅的 " + enums.getNAME() + " 更新了！");
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            msg.imgBase64(body.getFile());
        }
    }


}

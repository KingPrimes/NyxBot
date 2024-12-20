package com.nyx.bot.plugin.warframe.utils;

import com.alibaba.fastjson2.JSON;
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
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.warframe.subscribe.MissionSubscribeRepository;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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
        sendGroupsToUser(SubscribeEnums.NIGHTWAVE, I18nUtils.message("warframe.up.night-wave"));
    }

    /**
     * 突击
     */
    public static void updateSortie() {
        sendGroupsToUser(SubscribeEnums.SORTIE, I18nUtils.message("warframe.up.sortie"));
    }

    /**
     * 执政官突击
     */
    public static void updateArchonHunt() {
        sendGroupsToUser(SubscribeEnums.ARCHON_HUNT, I18nUtils.message("warframe.up.archonHunt"));
    }

    /**
     * 双衍王境
     */
    public static void updateDuviriCycle() {
        sendGroupsToUser(SubscribeEnums.DUVIRI_CYCLE, I18nUtils.message("warframe.up.duviriCycle"));
    }

    /**
     * 根据订阅类型返回图片Url后缀地址
     *
     * @param enums 订阅类型
     * @return 图片Url后缀地址
     */
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
            case ARCHON_HUNT -> {
                return "getArsonHuntImage";
            }
            case DUVIRI_CYCLE -> {
                return "getDuviriCycleImage";
            }
            case FISSURES -> {
                return "postSubscribeFissuresImage";
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
                        boolean flag = false;
                        List<MissionSubscribeUserCheckType> msucts = new ArrayList<>();
                        //获取是否是裂隙类型的订阅
                        for (MissionSubscribeUserCheckType userCheckType : user.getTypeList()) {
                            switch (userCheckType.getSubscribe()) {
                                case FISSURES -> {
                                    msucts.add(userCheckType);
                                }
                                case INVASIONS -> {
                                    flag = true;
                                }
                                case ARBITRATION -> {

                                }
                                case VOID, ALERTS, CETUS_CYCLE, DAILY_DEALS, STEEL_PATH, NEWS, NIGHTWAVE, SORTIE, ARCHON_HUNT, DUVIRI_CYCLE ->
                                        flag = true;
                            }
                        }
                        if (flag) {
                            ConstructTheReturnInformation(msg, enums, l, user.getUserId(), subGroup);
                            continue;
                        }
                        if (!msucts.isEmpty()) {
                            flag = ConstructTheReturnInformation(msg, enums, msucts, l, user.getUserId(), subGroup);
                        }
                        if (flag) {
                            bots.get(l).sendGroupMsg(subGroup, msg.at(user.getUserId()).text("您订阅的 " + enums.getNAME() + " 更新了！").build(), false);
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error("发送订阅消息失败：{}", e.getMessage(), e.getCause());
        }

    }

    /**
     * 发送更新提醒
     *
     * @param msg   消息体
     * @param enums 订阅类型
     * @param bot   机器人
     * @param user  用户
     * @param group 群组
     */
    static void ConstructTheReturnInformation(Msg msg, SubscribeEnums enums, Long bot, Long user, Long group) {
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                gestural(enums), new OneBotLogInfoData(
                        bot,
                        user,
                        group,
                        "",
                        DateUtils.getDate(),
                        PermissionsEnums.MANAGE,
                        Codes.WARFRAME_SUBSCRIBE,
                        "")
        );
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            msg.imgBase64(body.getFile());
        }
    }

    static boolean ConstructTheReturnInformation(Msg msg, SubscribeEnums enums, List<MissionSubscribeUserCheckType> msuct, Long bot, Long user, Long group) throws DataNotInfoException {
        var json = FissuresUtils.getFissures(msuct);
        List<GlobalStates.Fissures> t = CacheUtils.get(CacheUtils.GROUP_CAPTCHA, user.toString(), List.class);
        if (t != null && WarframeSubscribe.areListsEqual(json, t)) {
            return false;
        }
        CacheUtils.set(CacheUtils.GROUP_CAPTCHA, user.toString(), json);
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post(
                gestural(enums), new OneBotLogInfoData(
                        bot,
                        user,
                        group,
                        "",
                        DateUtils.getDate(),
                        PermissionsEnums.MANAGE,
                        Codes.WARFRAME_SUBSCRIBE,
                        JSON.toJSONString(json)
                )
        );
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            msg.imgBase64(body.getFile());
        }
        return true;
    }


}

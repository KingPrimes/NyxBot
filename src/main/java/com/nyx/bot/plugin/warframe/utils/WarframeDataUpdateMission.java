package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.MissionSubscribeRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WarframeDataUpdateMission {


    static MissionSubscribeRepository repository = SpringUtils.getBean(MissionSubscribeRepository.class);


    /**
     * 警报更新提醒
     */
    public static void updateAlerts() {
        sendGroupsToUser(SubscribeEnums.ALERTS, I18nUtils.message("warframe.up.alerts"), "");
    }

    /**
     * 仲裁更新提醒
     */
    public static void updateArbitration() {
        sendGroupsToUser(SubscribeEnums.ARBITRATION, I18nUtils.message("warframe.up.arbitration"), "");
    }

    /**
     * 每日特惠更新 提醒
     */
    public static void updateDailyDeals() {
        sendGroupsToUser(SubscribeEnums.DAILY_DEALS, I18nUtils.message("warframe.up.dayDeals"), "");
    }

    /**
     * 活动更新提醒
     */
    public static void updateEvents() {
        sendGroupsToUser(SubscribeEnums.EVENTS, I18nUtils.message("warframe.up.newEvents"), "");
    }

    /**
     * 新的入侵
     */
    public static void updateInvasions() {
        sendGroupsToUser(SubscribeEnums.INVASIONS, I18nUtils.message("warframe.up.invasions"), "");
    }

    /**
     * 钢铁之路兑换轮换
     */
    public static void updateSteelPath() {
        sendGroupsToUser(SubscribeEnums.STEEL_PATH, I18nUtils.message("warframe.up.steelPath"), "");
    }

    /**
     * 虚空商人 离开/到来 提醒
     *
     * @param msg 骚话！
     */
    public static void updateVoidTrader(String msg) {
        sendGroupsToUser(SubscribeEnums.VOID, msg, "");
    }

    /**
     * 到黑夜前提醒
     */
    public static void updateCetusCycle(String time) {
        sendGroupsToUser(SubscribeEnums.CETUS_CYCLE, I18nUtils.message("warframe.up.cetusCycle") + time, "");
    }

    /**
     * 通知所有的订阅群组与用户
     *
     * @param enums    通知类型
     * @param msgText  文本消息
     * @param imageUrl 图片Url地址
     */
    private static void sendGroupsToUser(SubscribeEnums enums, String msgText, String imageUrl) {

    }


}

package com.nyx.bot.plugin.warframe.utils;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.repo.warframe.MissionSubscribeRepository;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
     * 订阅全部消息
     *
     * @param subscribe 群组
     */
    public static void updateAll(MissionSubscribe subscribe) {
        List<MissionSubscribe> s = new ArrayList<>();
        for (SubscribeEnums enums : SubscribeEnums.values()) {
            subscribe.setSubscribe(enums);
            MissionSubscribe save = repository.save(subscribe);
            s.add(save);
        }
        if (s.size() == SubscribeEnums.values().length) {
            log.info("群组：{},用户：{} 已订阅所有消息", subscribe.getSubGroup(), subscribe.getSubUser());
        }
    }

    /**
     * 删除全部订阅消息
     *
     * @param subscribe 群组
     */
    public static void deleteAll(MissionSubscribe subscribe) {
        for (SubscribeEnums enums : SubscribeEnums.values()) {
            subscribe.setSubscribe(enums);
            repository.delete(subscribe);
        }
    }

    public static SubscribeEnums getSubscribeEnums(Integer or) {
        for (SubscribeEnums enums : SubscribeEnums.values()) {
            if (enums.ordinal() == or) {
                return enums;
            }
        }
        return SubscribeEnums.ERROR;
    }

    /**
     * 通知所有的订阅群组与用户
     *
     * @param enums    通知类型
     * @param msgText  文本消息
     * @param imageUrl 图片Url地址
     */
    private static void sendGroupsToUser(SubscribeEnums enums, String msgText, String imageUrl) {
        //msgText imageUrl 不可同时为空或null
        if ((Objects.equals(msgText, "") || msgText == null) && (Objects.equals(imageUrl, "") || imageUrl == null)) {
            return;
        }
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
                MsgUtils msg = new MsgUtils();
                //设置消息
                if (!Objects.equals(msgText, "")) {
                    msg.text(msgText + "\n");
                }
                if (!Objects.equals(imageUrl, "")) {
                    msg.img(imageUrl);
                }
                //如果user不为空则添加艾特 同时判断此用户是否开启订阅
                Optional.ofNullable(subscribe.getSubUser()).ifPresent(s -> {
                    if (subscribe.getSubscribe().equals(enums)) {
                        String[] users = subscribe.getSubUser().split("-");
                        for (String user : users) {
                            Optional.ofNullable(user).ifPresent(u -> {
                                msg.at(Long.parseLong(user));
                            });
                        }
                    }
                });

                if (subscribe.getSubscribe().equals(enums)) {
                    //bots.get(subscribe.getSubscriberBot()).sendGroupMsg(subscribe.getSubscribeGroup(),"",true);
                    //通知所有的订阅群组 同时判断此群组是否开启订阅
                    bots.get(subscribe.getSubBotUid()).sendGroupMsg(subscribe.getSubGroup(), msg.build(), false);
                    Thread.sleep(20);
                }

            }
        } catch (Exception e) {
            log.error("发送订阅消息失败：{}", e.getMessage());
        }

    }

}

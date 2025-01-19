package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class WarframeSubscribe {
    public static void isUpdated(SocketGlobalStates states) {
        isUpdated(states.getPacket().getData());
    }

    public static void isUpdated(GlobalStates states) {
        try {
            //获取缓存数据
            GlobalStates old = CacheUtils.getGlobalState();
            CacheUtils.setGlobalState(states);

            Optional.of(old).ifPresentOrElse(data -> {

                //检查警报是否不为空
                Optional.ofNullable(states.getAlerts()).ifPresent(f -> {
                    //判断数据是否有更新
                    if (!f.equals(data.getAlerts())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateAlerts);
                    }
                });

                //检查仲裁信息是否为空值
                Optional.ofNullable(states.getArbitration()).ifPresentOrElse(f -> {
                    if (!f.equals(data.getArbitration())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateArbitration);
                    }
                }, () -> {
                    //为空则使用缓存中的仲裁信息覆盖
                    states.setArbitration(data.getArbitration());
                });

                //每日特惠
                Optional.ofNullable(states.getDailyDeals()).ifPresent(r -> {
                    if (!r.equals(data.getDailyDeals())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateDailyDeals);
                    }
                });

                //活动
                Optional.ofNullable(states.getEvents()).ifPresent(events -> {
                    if (!events.equals(data.getEvents())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateEvents);
                    }
                });

                //裂隙
                Optional.ofNullable(states.getFissures()).ifPresent(fissures -> CompletableFuture
                        .supplyAsync(() -> areListsEqual(fissures, data.getFissures()))
                        .thenAccept(f -> {
                            if (!f) {
                                WarframeDataUpdateMission.updateFissures();
                            }
                        }));

                //入侵
                Optional.ofNullable(states.getInvasions()).ifPresent(invasions -> CompletableFuture
                        .supplyAsync(() -> areListsEqual(invasions, data.getInvasions()))
                        .thenAccept(f -> {
                            if (!f) {
                                WarframeDataUpdateMission.updateInvasions();
                            }
                        }));

                //新闻
                Optional.ofNullable(states.getNews()).ifPresent(news -> CompletableFuture
                        .supplyAsync(() -> areListsEqual(news, data.getNews()))
                        .thenAccept(f -> {
                            if (!f) {
                                WarframeDataUpdateMission.updateNews();
                            }
                        }));

                //电波
                Optional.ofNullable(states.getNightwave()).ifPresent(nightwave -> {
                    if (!nightwave.equals(data.getNightwave())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateNightwave);
                    }
                });
                //突击
                Optional.ofNullable(states.getSortie()).ifPresent(sortie -> {
                    if (!sortie.equals(data.getSortie())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateSortie);
                    }
                });
                //执政官突击
                Optional.ofNullable(states.getArchonHunt()).ifPresent(archonHunt -> {
                    if (!archonHunt.equals(data.getArchonHunt())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateArchonHunt);
                    }
                });
                //钢铁轮换
                Optional.ofNullable(states.getSteelPath()).ifPresent(steelPath -> {
                    if (!steelPath.equals(data.getSteelPath())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateSteelPath);
                    }
                });
                //虚空商人
                Optional.ofNullable(states.getVoidTrader()).ifPresent(voidTrader -> {
                    if (!voidTrader.equals(data.getVoidTrader())) {
                        if (voidTrader.getInventory().isEmpty() && !voidTrader.getActive()) {
                            CompletableFuture.runAsync(() -> WarframeDataUpdateMission.updateVoidTrader(I18nUtils.message("warframe.up.voidOut")));
                        } else {
                            CompletableFuture.runAsync(() -> WarframeDataUpdateMission.updateVoidTrader(I18nUtils.message("warframe.up.voidIn")));
                        }
                    }
                });
                //双衍王境
                Optional.ofNullable(states.getDuviriCycle()).ifPresent(duviriCycle -> {
                    if (!duviriCycle.equals(data.getDuviriCycle())) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateDuviriCycle);
                    }
                });

                //检查 夜灵平野 是否为空
                Optional.ofNullable(states.getCetusCycle()).ifPresent(f -> {
                    //判断是否更新
                    if (states.getCetusCycle().getState().toLowerCase(Locale.ROOT).equals("day")) {
                        //判断相差的小时是否在一小时以内
                        if (DateUtils.getDateHour(states.getCetusCycle().getExpiry(), new Date()) == 0) {
                            //取相差的分钟
                            long date = DateUtils.getDateMin(states.getCetusCycle().getExpiry(), new Date());
                            //相差13分钟的时候发送提醒
                            if (date == 13) {
                                //发送提醒
                                CompletableFuture.runAsync(() -> WarframeDataUpdateMission.updateCetusCycle(DateUtils.getDiff(states.getCetusCycle().getExpiry(), new Date())));
                            }
                        }
                    }
                });

            }, () -> {
                //更新数据
                CacheUtils.setGlobalState(states);
            });
        } catch (Exception e) {
            //设置数据
            CacheUtils.setGlobalState(states);
        }

    }

    /**
     * 深度比较List集合
     *
     * @param list1 第一个List
     * @param list2 第二个List
     * @param <T>   泛型
     * @return true表示两个List相等，false表示不相等
     */
    public static <T> boolean areListsEqual(List<T> list1, List<T> list2) {
        if (list1 == list2) {
            return true;
        }
        if (list1 == null || list2 == null || list1.size() != list2.size()) {
            return false;
        }
        for (T t : list1) {
            if (!list2.contains(t)) {
                return false;
            }
        }
        return true;
    }
}

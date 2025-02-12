package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.res.SocketGlobalStates;
import com.nyx.bot.utils.CacheUtils;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.nyx.bot.plugin.warframe.utils.GlobalStatesUtils.takeTheDifferenceSet;

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
                Optional.ofNullable(states.getAlerts()).ifPresent(a -> {
                    //判断数据是否有更新
                    CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getAlerts(), a))
                            .thenAccept(e -> {
                                if (!e.isEmpty()) {
                                    states.setAlerts(e);
                                    WarframeDataUpdateMission.updateAlerts(states);
                                }
                            });
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
                    if (!takeTheDifferenceSet(data.getDailyDeals(), r).isEmpty()) {
                        CompletableFuture.runAsync(WarframeDataUpdateMission::updateDailyDeals);
                    }
                });

                //活动
                Optional.ofNullable(states.getEvents()).ifPresent(events -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getEvents(), events))
                        .thenAccept(e -> {
                            if (!e.isEmpty()) {
                                states.setEvents(e);
                                WarframeDataUpdateMission.updateEvents(states);
                            }
                        }));

                //裂隙
                Optional.ofNullable(states.getFissures()).ifPresent(fissures -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getFissures(), fissures))
                        .thenAccept(f -> {
                            if (!f.isEmpty()) {
                                states.setFissures(f);
                                WarframeDataUpdateMission.updateFissures(states);
                            }
                        }));

                //入侵
                Optional.ofNullable(states.getInvasions()).ifPresent(invasions -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getInvasions(), invasions))
                        .thenAccept(i -> {
                            if (!i.isEmpty()) {
                                states.setInvasions(i);
                                WarframeDataUpdateMission.updateInvasions(states);
                            }
                        }));

                //新闻
                Optional.ofNullable(states.getNews()).ifPresent(news -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getNews(), news))
                        .thenAccept(n -> {
                            if (!n.isEmpty()) {
                                states.setNews(n);
                                WarframeDataUpdateMission.updateNews(states);
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
            log.error("WarframeDataSource.updateData error", e);
        }

    }
}

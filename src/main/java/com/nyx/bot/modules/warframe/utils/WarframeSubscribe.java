package com.nyx.bot.modules.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.modules.warframe.res.worldstate.DailyDeals;
import com.nyx.bot.modules.warframe.service.MissionSubscribeService;
import com.nyx.bot.utils.SpringUtils;
import com.nyx.bot.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.nyx.bot.modules.warframe.utils.GlobalStatesUtils.takeTheDifferenceSet;

@Slf4j
public class WarframeSubscribe {

    static MissionSubscribeService mss = SpringUtils.getBean(MissionSubscribeService.class);
    // 新增：记录最后一次触发Cetus周期通知的过期时间戳（初始值-1表示未触发过）
    private static long lastCetusExpiryNotified = -1;

    public static void isUpdated(WorldState states) {


        try {
            WorldState old = WarframeCache.getWarframeStatus();

            Optional.of(old).ifPresent(data -> {

                //每日特惠
                Optional.ofNullable(states.getDailyDeals()).ifPresent(r -> {
                    List<DailyDeals> list = r.stream().filter(i -> !old.getDailyDeals().stream().collect(Collectors.toMap(DailyDeals::getItem, value -> value)).containsKey(i.getItem())).toList();
                    if (!list.isEmpty()) {
                        mss.handleUpdate(SubscribeEnums.DAILY_DEALS, states);
                    }
                });

                //活动
                Optional.ofNullable(states.getGoals()).ifPresent(events -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getGoals(), events)).thenAccept(e -> {
                    if (!e.isEmpty()) {
                        states.setGoals(e);
                        mss.handleUpdate(SubscribeEnums.EVENTS, states);
                    }
                }));

                //裂隙
                Optional.ofNullable(states.getActiveMissions()).ifPresent(fissures -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getActiveMissions(), fissures)).thenAccept(f -> {
                    if (!f.isEmpty()) {
                        states.setActiveMissions(f);
                        mss.handleUpdate(SubscribeEnums.FISSURES, states);
                    }
                }));

                //入侵
                Optional.ofNullable(states.getInvasions()).ifPresent(invasions -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getInvasions(), invasions)).thenAccept(i -> {
                    if (!i.isEmpty()) {
                        states.setInvasions(i);
                        mss.handleUpdate(SubscribeEnums.INVASIONS, states);
                    }
                }));

                //新闻
                Optional.ofNullable(states.getEvents()).ifPresent(news -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getEvents(), news)).thenAccept(n -> {
                    if (!n.isEmpty()) {
                        states.setEvents(n);
                        mss.handleUpdate(SubscribeEnums.NEWS, states);
                    }
                }));

                //电波
                Optional.ofNullable(states.getSeasonInfo()).ifPresent(nightwave -> {
                    if (!nightwave.getSeason().equals(data.getSeasonInfo().getSeason())) {
                        mss.handleUpdate(SubscribeEnums.NIGHTWAVE, states);
                    }
                });
                //突击
                Optional.ofNullable(states.getSorties()).ifPresent(sortie -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getSorties(), sortie)).thenAccept(s -> {
                    if (!s.isEmpty()) {
                        mss.handleUpdate(SubscribeEnums.SORTIE, states);
                    }
                }));

                //执政官突击
                Optional.ofNullable(states.getLiteSorties()).ifPresent(sortie -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getLiteSorties(), sortie)).thenAccept(s -> {
                    if (!s.isEmpty()) {
                        mss.handleUpdate(SubscribeEnums.ARCHON_HUNT, states);
                    }
                }));

                //虚空商人
                Optional.ofNullable(states.getVoidTraders()).ifPresent(voidTraders -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getVoidTraders(), voidTraders)).thenAccept(s -> {
                    if (!s.isEmpty()) {
                        mss.handleUpdate(SubscribeEnums.VOID, states);
                    }
                }));
                // 钢铁兑换
                Optional.ofNullable(states.getSteelPath()).ifPresent(steelPath -> CompletableFuture.runAsync(() -> {
                    if (!old.getSteelPath().getNextReward().getName().equals(steelPath.getNextReward().getName())) {
                        mss.handleUpdate(SubscribeEnums.STEEL_PATH, states);
                    }
                }));

                // 夜灵平原
                Optional.ofNullable(states.getCetusCycle()).ifPresent(cetusCycle -> CompletableFuture.runAsync(() -> {
                    // 获取当前周期的过期时间戳（用于判断是否为新周期）
                    long currentExpiry = old.getCetusCycle().getExpiry().getEpochSecond() * 1000;
                    long minutes = TimeUtils.timeDeltaToMinutes(currentExpiry * 1000);
                    // 条件：1. 剩余时间<=18分钟  2. 当前周期未触发过通知
                    if (minutes <= 18 && lastCetusExpiryNotified != currentExpiry) {
                        mss.handleUpdate(SubscribeEnums.CETUS_CYCLE, states);
                        // 记录当前周期的过期时间戳，标记为已触发
                        lastCetusExpiryNotified = currentExpiry;
                    }
                }));

            });

        } catch (DataNotInfoException e) {
            log.error("DataNotInfoException:{}", e.getMessage());
        }
    }
}

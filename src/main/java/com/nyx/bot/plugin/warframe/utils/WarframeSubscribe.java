package com.nyx.bot.plugin.warframe.utils;

import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.exception.DataNotInfoException;
import com.nyx.bot.repo.impl.warframe.MissionSubscribeService;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.res.worldstate.DailyDeals;
import com.nyx.bot.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.nyx.bot.plugin.warframe.utils.GlobalStatesUtils.takeTheDifferenceSet;

@Slf4j
public class WarframeSubscribe {

    static MissionSubscribeService mss = SpringUtils.getBean(MissionSubscribeService.class);

    public static void isUpdated(WorldState states) {


        try {
            WorldState old = WarframeCache.getWarframeStatus();

            Optional.of(old).ifPresent(data -> {

                //每日特惠
                Optional.ofNullable(states.getDailyDeals()).ifPresent(r -> {
                    List<DailyDeals> list = r.stream()
                            .filter(i -> !old.getDailyDeals().stream()
                                    .collect(Collectors.toMap(DailyDeals::getItem, value -> value))
                                    .containsKey(i.getItem())).toList();
                    if (!list.isEmpty()) {
                        mss.handleUpdate(SubscribeEnums.DAILY_DEALS, states);
                    }
                });

                //活动
                Optional.ofNullable(states.getGoals()).ifPresent(events -> CompletableFuture.supplyAsync(() -> takeTheDifferenceSet(data.getGoals(), events))
                        .thenAccept(e -> {
                            if (!e.isEmpty()) {
                                states.setGoals(e);
                                mss.handleUpdate(SubscribeEnums.EVENTS, states);
                            }
                        }));

                //裂隙
                Optional.ofNullable(states.getActiveMissions()).ifPresent(fissures -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getActiveMissions(), fissures))
                        .thenAccept(f -> {
                            if (!f.isEmpty()) {
                                states.setActiveMissions(f);
                                mss.handleUpdate(SubscribeEnums.FISSURES, states);
                            }
                        }));

                //入侵
                Optional.ofNullable(states.getInvasions()).ifPresent(invasions -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getInvasions(), invasions))
                        .thenAccept(i -> {
                            if (!i.isEmpty()) {
                                states.setInvasions(i);
                                mss.handleUpdate(SubscribeEnums.INVASIONS, states);
                            }
                        }));

                //新闻
                Optional.ofNullable(states.getEvents()).ifPresent(news -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getEvents(), news))
                        .thenAccept(n -> {
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
                Optional.ofNullable(states.getSorties()).ifPresent(sortie -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getSorties(), sortie))
                        .thenAccept(s -> {
                            if (!s.isEmpty()) {
                                mss.handleUpdate(SubscribeEnums.SORTIE, states);
                            }
                        }));

                //执政官突击
                Optional.ofNullable(states.getLiteSorties())
                        .ifPresent(sortie -> CompletableFuture
                                .supplyAsync(() -> takeTheDifferenceSet(data.getLiteSorties(), sortie))
                                .thenAccept(s -> {
                                    if (!s.isEmpty()) {
                                        mss.handleUpdate(SubscribeEnums.ARCHON_HUNT, states);
                                    }
                                }));

                //虚空商人
                Optional.ofNullable(states.getVoidTraders()).ifPresent(voidTraders -> CompletableFuture
                        .supplyAsync(() -> takeTheDifferenceSet(data.getVoidTraders(), voidTraders))
                        .thenAccept(s -> {
                            if (!s.isEmpty()) {
                                mss.handleUpdate(SubscribeEnums.VOID, states);
                            }
                        }));

            });

        } catch (DataNotInfoException e) {
            log.error("DataNotInfoException:{}", e.getMessage());
        }
    }
}

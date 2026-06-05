package com.nyx.bot.modules.warframe.application;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.entity.NotificationHistory;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationApplicationService {

    /**
     * 订阅推送类型（需要按用户规则过滤 missionType/tier）
     */
    private static final Set<SubscribeType> SUBSCRIPTION_TYPES = EnumSet.of(
            SubscribeType.FISSURES, SubscribeType.INVASIONS
    );
    private final Map<SubscribeType, ChangeDetector<?>> detectors;
    private final Map<SubscribeType, MessageBuilder<?>> builders;
    private final MissionSubscribeRepository subscribeRepo;
    private final NotificationHistoryRepository historyRepo;
    private final BotContainer botContainer;
    private final ExecutorService taskExecutor;

    public NotificationApplicationService(
            List<ChangeDetector<?>> detectorList,
            List<MessageBuilder<?>> builderList,
            MissionSubscribeRepository subscribeRepo,
            NotificationHistoryRepository historyRepo,
            BotContainer botContainer,
            ExecutorService taskExecutor
    ) {
        this.detectors = detectorList.stream()
                .collect(Collectors.toMap(ChangeDetector::getSupportedType, Function.identity()));
        this.builders = builderList.stream()
                .collect(Collectors.toMap(MessageBuilder::getSupportedType, Function.identity()));
        this.subscribeRepo = subscribeRepo;
        this.historyRepo = historyRepo;
        this.botContainer = botContainer;
        this.taskExecutor = taskExecutor;
        log.info("NotificationApplicationService 初始化完成");
        log.info("已注册 {} 个 ChangeDetector: {}", detectors.size(), detectors.keySet());
        log.info("已注册 {} 个 MessageBuilder: {}", builders.size(), builders.keySet());
    }

    public void handleStateUpdate(WorldState oldState, WorldState newState) {
        if (oldState == null || newState == null) {
            log.debug("状态为空，跳过处理");
            return;
        }
        List<ChangeEvent<?>> allChanges = detectAllChanges(oldState, newState);
        if (allChanges.isEmpty()) {
            log.debug("未检测到任何变化");
            return;
        }
        log.debug("检测到 {} 个变化事件", allChanges.size());
        Map<SubscribeType, List<ChangeEvent<?>>> changesByType = allChanges.stream()
                .collect(Collectors.groupingBy(ChangeEvent::type));
        changesByType.forEach((type, changes) ->
                CompletableFuture.runAsync(() -> notifySubscribers(type, changes), taskExecutor)
                        .exceptionally(ex -> {
                            log.error("通知推送异常 [type:{}]", type.getName(), ex);
                            return null;
                        })
        );
    }

    private List<ChangeEvent<?>> detectAllChanges(WorldState oldState, WorldState newState) {
        return detectors.values().stream()
                .flatMap(detector -> {
                    try {
                        detector.cleanExpiredHistory();
                        List<? extends ChangeEvent<?>> changes = detector.detectChanges(oldState, newState);
                        if (!changes.isEmpty()) {
                            log.debug("Detector {} 检测到 {} 个变化",
                                    detector.getClass().getSimpleName(), changes.size());
                        }
                        return changes.stream();
                    } catch (Exception e) {
                        log.error("Detector {} 执行失败", detector.getClass().getSimpleName(), e);
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 通知订阅者，区分主动推送和订阅推送
     */
    private void notifySubscribers(SubscribeType type, List<ChangeEvent<?>> changes) {
        List<MissionSubscribe> subscriptions = subscribeRepo.findSubscriptions(type);
        if (subscriptions.isEmpty()) {
            log.debug("类型 {} 没有订阅者", type.getName());
            return;
        }
        log.debug("类型 {} 有 {} 个订阅组", type.getName(), subscriptions.size());

        if (SUBSCRIPTION_TYPES.contains(type)) {
            notifyBySubscription(subscriptions, type, changes);
        } else {
            notifyByActivePush(subscriptions, type, changes);
        }
    }

    /**
     * 主动推送：按群合并消息，一条消息 @所有相关用户
     */
    private void notifyByActivePush(List<MissionSubscribe> subscriptions,
                                    SubscribeType type, List<ChangeEvent<?>> changes) {
        MessageBuilder<?> builder = builders.get(type);
        if (builder == null) {
            log.warn("类型 {} 没有对应的 MessageBuilder", type.getName());
            return;
        }

        for (MissionSubscribe sub : subscriptions) {
            try {
                // 收集该群所有订阅此类型的用户
                List<Long> userIds = sub.getUsers().stream()
                        .filter(u -> u.getCheckTypes().stream().anyMatch(r -> r.getSubscribe() == type))
                        .map(MissionSubscribeUser::getUserId)
                        .toList();
                if (userIds.isEmpty()) {
                    continue;
                }

                Bot bot = botContainer.robots.get(sub.getSubBotUid());
                if (bot == null) {
                    log.warn("Bot {} 不存在，跳过通知", sub.getSubBotUid());
                    continue;
                }

                ArrayMsgUtils msg = ArrayMsgUtils.builder();
                userIds.forEach(msg::at);
                msg.text("您订阅的 " + type.getName() + " 已更新！");

                for (ChangeEvent<?> change : changes) {
                    Long expiry = extractExpiry(change);
                    if (expiry != null && historyRepo.existsBySubscribeTypeAndExpiryTimestamp(type, expiry)) {
                        log.debug("重复通知已跳过 [type:{}] [expiry:{}]", type.getName(), expiry);
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    ChangeEvent<Object> rawChange = (ChangeEvent<Object>) change;
                    MessageBuilder<Object> rawBuilder = (MessageBuilder<Object>) builder;
                    ArrayMsgUtils eventMsg = rawBuilder.buildMessage(rawChange, null);
                    msg.text(eventMsg.buildCQ());
                    saveHistory(type, expiry);
                }

                bot.sendGroupMsg(sub.getSubGroup(), msg.buildCQ(), false);
                log.debug("通知发送成功 [bot:{}] [group:{}] [type:{}] [users:{}]",
                        sub.getSubBotUid(), sub.getSubGroup(), type.getName(), userIds.size());
            } catch (Exception e) {
                log.error("通知发送失败 [group:{}] [type:{}]", sub.getSubGroup(), type.getName(), e);
            }
        }
    }

    /**
     * 订阅推送：按用户规则过滤，同群同类型合并为一条消息
     */
    private void notifyBySubscription(List<MissionSubscribe> subscriptions,
                                      SubscribeType type, List<ChangeEvent<?>> changes) {
        MessageBuilder<?> builder = builders.get(type);
        if (builder == null) {
            log.warn("类型 {} 没有对应的 MessageBuilder", type.getName());
            return;
        }

        for (MissionSubscribe sub : subscriptions) {
            try {
                List<Long> matchedUsers = new ArrayList<>();
                ArrayMsgUtils msg = ArrayMsgUtils.builder();

                for (MissionSubscribeUser user : sub.getUsers()) {
                    List<ChangeEvent<?>> matched = filterMatchingChanges(user, changes);
                    if (matched.isEmpty()) {
                        continue;
                    }
                    matchedUsers.add(user.getUserId());
                    msg.at(user.getUserId());

                    for (ChangeEvent<?> change : matched) {
                        Long expiry = extractExpiry(change);
                        if (expiry != null && historyRepo.existsBySubscribeTypeAndExpiryTimestamp(type, expiry)) {
                            continue;
                        }
                        MissionSubscribeUserCheckType matchingRule = user.getCheckTypes().stream()
                                .filter(rule -> matchesRule(change, rule))
                                .findFirst().orElse(null);
                        @SuppressWarnings("unchecked")
                        ChangeEvent<Object> rawChange = (ChangeEvent<Object>) change;
                        MessageBuilder<Object> rawBuilder = (MessageBuilder<Object>) builder;
                        ArrayMsgUtils eventMsg = rawBuilder.buildMessage(rawChange, matchingRule);
                        msg.text(eventMsg.buildCQ());
                        saveHistory(type, expiry);
                    }
                }

                if (matchedUsers.isEmpty()) {
                    continue;
                }

                Bot bot = botContainer.robots.get(sub.getSubBotUid());
                if (bot == null) {
                    log.warn("Bot {} 不存在，跳过通知", sub.getSubBotUid());
                    continue;
                }

                msg.text("您订阅的 " + type.getName() + " 已更新！");
                bot.sendGroupMsg(sub.getSubGroup(), msg.buildCQ(), false);
                log.debug("通知发送成功 [bot:{}] [group:{}] [type:{}] [users:{}]",
                        sub.getSubBotUid(), sub.getSubGroup(), type.getName(), matchedUsers.size());
            } catch (Exception e) {
                log.error("通知发送失败 [group:{}] [type:{}]", sub.getSubGroup(), type.getName(), e);
            }
        }
    }

    private List<ChangeEvent<?>> filterMatchingChanges(MissionSubscribeUser user, List<ChangeEvent<?>> changes) {
        return changes.stream()
                .filter(event -> user.getCheckTypes().stream().anyMatch(rule -> matchesRule(event, rule)))
                .collect(Collectors.toList());
    }

    private boolean matchesRule(ChangeEvent<?> event, MissionSubscribeUserCheckType rule) {
        if (rule.getSubscribe() != event.type()) {
            return false;
        }
        if (rule.getMissionTypeEnum() != null) {
            if (event.missionType() == null || !rule.getMissionTypeEnum().equals(event.missionType())) {
                return false;
            }
        }
        if (rule.getTierNum() != null) {
            return event.tier() != null && rule.getTierNum().equals(event.tier());
        }
        if (rule.getInvasionReward() != null) {
            return event.invasionReward() != null && rule.getInvasionReward() == event.invasionReward();
        }
        return true;
    }

    /**
     * 从 ChangeEvent.data 中提取过期时间戳用于去重。
     * 所有 WorldState 数据模型都继承 BastWorldState，通过 getExpiry() 获取。
     */
    private Long extractExpiry(ChangeEvent<?> event) {
        try {
            if (event.data() instanceof BastWorldState bws && bws.getExpiry() != null) {
                return bws.getExpiry().getEpochSecond().getEpochSecond();
            }
        } catch (Exception e) {
            log.debug("提取 expiry 失败: {}", e.getMessage());
        }
        return null;
    }

    private void saveHistory(SubscribeType type, Long expiryTimestamp) {
        if (expiryTimestamp == null) return;
        try {
            NotificationHistory history = new NotificationHistory();
            history.setSubscribeType(type);
            history.setExpiryTimestamp(expiryTimestamp);
            history.setNotifiedAt(Instant.now());
            historyRepo.save(history);
        } catch (Exception e) {
            log.error("保存通知历史失败 [type:{}] [expiry:{}]", type.getName(), expiryTimestamp, e);
        }
    }
}

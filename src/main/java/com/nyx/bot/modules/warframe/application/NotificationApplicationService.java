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
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通知应用服务
 * 负责检测游戏状态变化并发送通知
 * <p>
 * 核心职责：
 * 1. 协调所有 ChangeDetector 检测变化
 * 2. 根据订阅规则精确过滤
 * 3. 使用 MessageBuilder 构建消息
 * 4. 通过 Bot 发送群消息
 *
 */
@Slf4j
@Service
public class NotificationApplicationService {

    private final Map<SubscribeEnums, ChangeDetector> detectors;
    private final Map<SubscribeEnums, MessageBuilder> builders;
    private final MissionSubscribeRepository subscribeRepo;
    private final BotContainer botContainer;

    /**
     * 构造函数 - Spring 自动注入所有实现类
     *
     * @param detectorList  所有 ChangeDetector 实现（通过 @Component 自动扫描）
     * @param builderList   所有 MessageBuilder 实现（通过 @Component 自动扫描）
     * @param subscribeRepo 订阅仓储
     * @param botContainer  Bot 容器
     */
    @Autowired
    public NotificationApplicationService(
            List<ChangeDetector> detectorList,
            List<MessageBuilder> builderList,
            MissionSubscribeRepository subscribeRepo,
            BotContainer botContainer
    ) {
        // 将 List 转换为 Map，key 为订阅类型，value 为实现类
        this.detectors = detectorList.stream()
                .collect(Collectors.toMap(
                        ChangeDetector::getSupportedType,
                        Function.identity()
                ));

        this.builders = builderList.stream()
                .collect(Collectors.toMap(
                        MessageBuilder::getSupportedType,
                        Function.identity()
                ));

        this.subscribeRepo = subscribeRepo;
        this.botContainer = botContainer;

        log.info("NotificationApplicationService 初始化完成");
        log.info("已注册 {} 个 ChangeDetector: {}", detectors.size(), detectors.keySet());
        log.info("已注册 {} 个 MessageBuilder: {}", builders.size(), builders.keySet());
    }

    /**
     * 处理游戏状态更新
     * 这是新架构的核心入口方法
     *
     * @param oldState 旧状态
     * @param newState 新状态
     */
    public void handleStateUpdate(WorldState oldState, WorldState newState) {
        if (oldState == null || newState == null) {
            log.debug("状态为空，跳过处理");
            return;
        }

        // 1. 使用所有 Detector 检测变化
        List<ChangeEvent> allChanges = detectAllChanges(oldState, newState);

        if (allChanges.isEmpty()) {
            log.debug("未检测到任何变化");
            return;
        }

        log.info("检测到 {} 个变化事件", allChanges.size());

        // 2. 按订阅类型分组
        Map<SubscribeEnums, List<ChangeEvent>> changesByType = allChanges.stream()
                .collect(Collectors.groupingBy(ChangeEvent::getType));

        // 3. 异步处理每种类型的通知
        changesByType.forEach((type, changes) ->
                CompletableFuture.runAsync(() -> notifySubscribers(type, changes))
        );
    }

    /**
     * 检测所有类型的变化
     */
    private List<ChangeEvent> detectAllChanges(WorldState oldState, WorldState newState) {
        return detectors.values().stream()
                .flatMap(detector -> {
                    try {
                        List<ChangeEvent> changes = detector.detectChanges(oldState, newState);
                        if (!changes.isEmpty()) {
                            log.debug("Detector {} 检测到 {} 个变化",
                                    detector.getClass().getSimpleName(), changes.size());
                        }
                        return changes.stream();
                    } catch (Exception e) {
                        log.error("Detector {} 执行失败",
                                detector.getClass().getSimpleName(), e);
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 通知订阅者
     * 核心逻辑：精确过滤 + 构建消息 + 发送通知
     *
     * @param type    订阅类型
     * @param changes 变化列表
     */
    private void notifySubscribers(SubscribeEnums type, List<ChangeEvent> changes) {
        // 查询订阅该类型的所有订阅组
        List<MissionSubscribe> subscriptions = subscribeRepo.findSubscriptions(type);

        if (subscriptions.isEmpty()) {
            log.debug("类型 {} 没有订阅者", type.getNAME());
            return;
        }

        log.info("类型 {} 有 {} 个订阅组", type.getNAME(), subscriptions.size());

        // 遍历订阅组
        subscriptions.forEach(subscription -> {
            // 遍历订阅组中的用户
            subscription.getUsers().forEach(user -> {
                // ⭐ 核心：精确匹配用户的订阅规则
                List<ChangeEvent> matchedChanges = filterMatchingChanges(user, changes);

                if (!matchedChanges.isEmpty()) {
                    sendNotification(subscription, user, matchedChanges);
                }
            });
        });
    }

    /**
     * 精确过滤匹配的变化
     * 检查：订阅类型 + 任务类型 + 遗物等级
     *
     * @param user    订阅用户
     * @param changes 所有变化
     * @return 匹配的变化列表
     */
    private List<ChangeEvent> filterMatchingChanges(
            MissionSubscribeUser user,
            List<ChangeEvent> changes
    ) {
        return changes.stream()
                .filter(event -> user.getCheckTypes().stream()
                        .anyMatch(rule -> matchesRule(event, rule)))
                .collect(Collectors.toList());
    }

    /**
     * 判断事件是否匹配规则
     * 三维精确匹配：类型 + 任务类型 + 等级
     */
    private boolean matchesRule(ChangeEvent event, MissionSubscribeUserCheckType rule) {
        // 1. 订阅类型必须匹配
        if (rule.getSubscribe() != event.getType()) {
            return false;
        }

        // 2. 任务类型匹配（null 表示全部）
        if (rule.getMissionTypeEnum() != null &&
                event.getMissionType() != null &&
                rule.getMissionTypeEnum() != event.getMissionType()) {
            return false;
        }

        // 3. 遗物等级匹配（null 表示全部）
        if (rule.getTierNum() != null &&
                event.getTier() != null &&
                !rule.getTierNum().equals(event.getTier())) {
            return false;
        }

        return true;
    }

    /**
     * 发送通知
     * 使用 MessageBuilder 构建消息并通过 Bot 发送
     *
     * @param subscription 订阅组
     * @param user         订阅用户
     * @param changes      匹配的变化列表
     */
    private void sendNotification(
            MissionSubscribe subscription,
            MissionSubscribeUser user,
            List<ChangeEvent> changes
    ) {
        try {
            // 获取 Bot 实例
            Bot bot = botContainer.robots.get(subscription.getSubBotUid());
            if (bot == null) {
                log.warn("Bot {} 不存在，跳过通知", subscription.getSubBotUid());
                return;
            }

            // 获取 MessageBuilder
            SubscribeEnums type = changes.getFirst().getType();
            MessageBuilder builder = builders.get(type);
            if (builder == null) {
                log.warn("类型 {} 没有对应的 MessageBuilder", type.getNAME());
                return;
            }

            // 构建消息头
            ArrayMsgUtils msg = ArrayMsgUtils.builder()
                    .at(user.getUserId())
                    .text("您订阅的 " + type.getNAME() + " 已更新！");

            // 为每个变化构建消息内容
            for (ChangeEvent change : changes) {
                // 找到匹配的规则（用于个性化消息）
                MissionSubscribeUserCheckType matchingRule = user.getCheckTypes().stream()
                        .filter(rule -> matchesRule(change, rule))
                        .findFirst()
                        .orElse(null);

                if (matchingRule != null) {
                    ArrayMsgUtils eventMsg = builder.buildMessage(change, matchingRule);
                    msg.text(eventMsg.buildCQ());
                }
            }

            // 发送群消息
            bot.sendGroupMsg(subscription.getSubGroup(), msg.buildCQ(), false);

            log.info("通知发送成功 [bot:{}] [group:{}] [user:{}] [type:{}] [count:{}]",
                    subscription.getSubBotUid(),
                    subscription.getSubGroup(),
                    user.getUserId(),
                    type.getNAME(),
                    changes.size());

        } catch (Exception e) {
            log.error("通知发送失败 [group:{}] [user:{}]",
                    subscription.getSubGroup(), user.getUserId(), e);
        }
    }
}
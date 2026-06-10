package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.NotificationHistory;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.utils.TimeUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SyndicateEnum;
import io.github.kingprimes.model.worldstate.CetusCycle;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * 夜灵平原周期变化检测器
 * 检测地球平原的昼夜循环变化
 * <p>
 * 触发条件：剩余时间 <= 18 分钟时通知一次
 * <p>
 * 防重复机制：使用数据库记录已通知的周期，通过过期时间戳唯一标识每个周期
 */
@Slf4j
@Component
public class CetusCycleChangeDetector implements ChangeDetector<CetusCycle> {

    private final NotificationHistoryRepository historyRepository;

    /**
     * 历史记录保留时长（小时）
     * 默认 12 小时，从配置文件读取
     */
    @Value("${warframe.notification.history.retention-hours:12}")
    private int retentionHours;

    /**
     * 缓存的保留时长
     * 避免每次调用都创建 Duration 对象
     */
    private Duration retentionDuration;

    public CetusCycleChangeDetector(NotificationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    /**
     * 初始化方法，在依赖注入完成后执行
     */
    @PostConstruct
    private void init() {
        this.retentionDuration = Duration.ofHours(retentionHours);
        log.info("夜灵平原周期检测器初始化完成 [历史记录保留时长:{}小时]", retentionHours);
    }

    @Override
    public List<ChangeEvent<CetusCycle>> detectChanges(WorldState oldState, WorldState newState) {
        // 边界检查
        if (newState == null || newState.getCetusCycle() == null) {
            log.debug("新状态或夜灵平原数据为空");
            return Collections.emptyList();
        }

        CetusCycle cetusCycle = newState.getCetusCycle();

        // 使用赏金任务的过期时间（API 固定值）作为周期唯一标识，而非 CetusCycle 的计算值
        Instant bountyExpiry = java.util.Optional.ofNullable(newState.getSyndicateMissions()).orElse(List.of()).stream()
                .filter(s -> s.getTag() != null && s.getTag() == SyndicateEnum.CetusSyndicate)
                .findFirst()
                .map(s -> s.getExpiry() == null ? null : s.getExpiry().getEpochSecond())
                .orElse(null);
        if (bountyExpiry == null) {
            log.debug("未找到 Cetus 赏金任务数据，跳过检测");
            return Collections.emptyList();
        }
        long expiryTimestamp = bountyExpiry.getEpochSecond();

        // 计算剩余时间（分钟）
        long remainingMinutes = TimeUtils.timeDeltaToMinutes(expiryTimestamp * 1000);

        // 触发条件：
        // 1. 剩余时间 <= 18 分钟
        // 2. 当前周期还未通知过（通过数据库检查）
        if (remainingMinutes <= 18) {
            // 检查数据库中是否已存在该周期的通知记录
            boolean alreadyNotified = historyRepository.existsBySubscribeTypeAndExpiryTimestamp(
                    SubscribeType.CETUS_CYCLE,
                    expiryTimestamp
            );

            if (alreadyNotified) {
                log.debug("夜灵平原周期已通知过 [expiry:{}] [剩余:{}分钟]",
                        expiryTimestamp, remainingMinutes);
                return Collections.emptyList();
            }

            log.info("检测到夜灵平原周期即将结束 [状态:{}] [剩余:{}分钟] [expiry:{}]",
                    cetusCycle.getCycle(), remainingMinutes, expiryTimestamp);

            // 保存通知记录到数据库
            NotificationHistory history = new NotificationHistory();
            history.setSubscribeType(SubscribeType.CETUS_CYCLE);
            history.setExpiryTimestamp(expiryTimestamp);
            history.setNotifiedAt(Instant.now());
            history.setCycleState(cetusCycle.getCycle());
            historyRepository.save(history);

            log.info("已记录夜灵平原周期通知历史 [expiry:{}]", expiryTimestamp);

            ChangeEvent<CetusCycle> event = ChangeEvent.of(
                    SubscribeType.CETUS_CYCLE,
                    null,  // 无任务类型
                    null,  // 无等级
                    cetusCycle
            );

            return List.of(event);
        }

        return Collections.emptyList();
    }

    /**
     * 清理当前订阅类型的过期历史记录
     * <p>
     * 清理规则：删除 notifiedAt 早于 (当前时间 - 保留时长) 的记录
     * </p>
     */
    @Override
    @Transactional
    public void cleanExpiredHistory() {
        try {
            // 计算过期时间点：当前时间 - 保留时长
            Instant expiredBefore = Instant.now().minus(retentionDuration);

            // 删除过期记录（Repository 方法自带事务）
            long deletedCount = historyRepository.deleteBySubscribeTypeAndNotifiedAtBefore(
                    SubscribeType.CETUS_CYCLE,
                    expiredBefore
            );

            if (deletedCount > 0) {
                log.debug("清理夜灵平原周期过期通知记录 [数量:{}] [过期时间:{}]",
                        deletedCount, expiredBefore);
            }
        } catch (Exception e) {
            log.error("清理夜灵平原周期历史记录失败", e);
        }
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.CETUS_CYCLE;
    }
}
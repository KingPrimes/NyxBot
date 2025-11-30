package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.NotificationHistory;
import com.nyx.bot.modules.warframe.repo.NotificationHistoryRepository;
import com.nyx.bot.utils.TimeUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.CetusCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class CetusCycleChangeDetector implements ChangeDetector {

    private final NotificationHistoryRepository historyRepository;

    public CetusCycleChangeDetector(NotificationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState) {
        // 边界检查
        if (newState == null || newState.getCetusCycle() == null) {
            log.debug("新状态或夜灵平原数据为空");
            return Collections.emptyList();
        }

        CetusCycle cetusCycle = newState.getCetusCycle();

        // 获取当前周期的过期时间戳（秒）
        long expiryTimestamp = cetusCycle.getExpiry().getEpochSecond();

        // 计算剩余时间（分钟）
        long remainingMinutes = TimeUtils.timeDeltaToMinutes(expiryTimestamp * 1000);

        // 触发条件：
        // 1. 剩余时间 <= 18 分钟
        // 2. 当前周期还未通知过（通过数据库检查）
        if (remainingMinutes <= 18) {
            // 检查数据库中是否已存在该周期的通知记录
            boolean alreadyNotified = historyRepository.existsBySubscribeTypeAndExpiryTimestamp(
                    SubscribeEnums.CETUS_CYCLE,
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
            history.setSubscribeType(SubscribeEnums.CETUS_CYCLE);
            history.setExpiryTimestamp(expiryTimestamp);
            history.setNotifiedAt(Instant.now());
            history.setCycleState(cetusCycle.getCycle());
            historyRepository.save(history);

            log.info("已记录夜灵平原周期通知历史 [expiry:{}]", expiryTimestamp);

            ChangeEvent event = ChangeEvent.of(
                    SubscribeEnums.CETUS_CYCLE,
                    null,  // 无任务类型
                    null,  // 无等级
                    cetusCycle
            );

            return List.of(event);
        }

        return Collections.emptyList();
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.CETUS_CYCLE;
    }
}
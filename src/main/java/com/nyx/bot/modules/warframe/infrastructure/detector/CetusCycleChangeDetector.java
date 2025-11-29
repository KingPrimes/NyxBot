package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.utils.TimeUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.CetusCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 夜灵平原周期变化检测器
 * 检测地球平原的昼夜循环变化
 * <p>
 * 触发条件：剩余时间 <= 18 分钟时通知一次
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class CetusCycleChangeDetector implements ChangeDetector {

    // 记录最后一次通知的过期时间戳，避免重复通知
    private static long lastNotifiedExpiry = -1;

    @Override
    public List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState) {
        // 边界检查
        if (newState == null || newState.getCetusCycle() == null) {
            log.debug("新状态或夜灵平原数据为空");
            return Collections.emptyList();
        }

        CetusCycle cetusCycle = newState.getCetusCycle();

        // 获取当前周期的过期时间戳（秒 -> 毫秒）
        long currentExpiry = cetusCycle.getExpiry().getEpochSecond() * 1000;

        // 计算剩余时间（分钟）
        long remainingMinutes = TimeUtils.timeDeltaToMinutes(currentExpiry);

        // 触发条件：
        // 1. 剩余时间 <= 18 分钟
        // 2. 当前周期还未通知过
        if (remainingMinutes <= 18 && lastNotifiedExpiry != currentExpiry) {
            log.info("检测到夜灵平原周期即将结束 [状态:{}] [剩余:{}分钟]",
                    cetusCycle.getCycle(), remainingMinutes);

            // 记录当前周期，避免重复通知
            // TODO : 避免重复通知无作用
            lastNotifiedExpiry = currentExpiry;

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
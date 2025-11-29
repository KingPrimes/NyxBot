package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.Alert;
import io.github.kingprimes.model.worldstate.BastWorldState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 警报变化检测器
 * 检测新出现的警报任务
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class AlertsChangeDetector implements ChangeDetector {

    @Override
    public List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState) {
        // 1. 边界检查
        if (newState == null || newState.getAlerts() == null) {
            log.debug("新状态或警报数据为空");
            return Collections.emptyList();
        }

        if (oldState == null || oldState.getAlerts() == null) {
            log.debug("旧状态为空，跳过警报检测");
            return Collections.emptyList();
        }

        // 2. 提取旧警报ID集合
        Set<BastWorldState.Id> oldAlertIds = oldState.getAlerts().stream()
                .map(Alert::get_id)
                .collect(Collectors.toSet());

        // 3. 过滤出新增的警报（且有任务信息的）
        List<ChangeEvent> changes = newState.getAlerts().stream()
                .filter(alert -> alert.getMissionInfo() != null)  // 必须有任务信息
                .filter(alert -> !oldAlertIds.contains(alert.get_id()))
                .map(this::createChangeEvent)
                .collect(Collectors.toList());

        if (!changes.isEmpty()) {
            log.info("检测到 {} 个新警报", changes.size());
        }

        return changes;
    }

    /**
     * 创建警报变化事件
     * 警报有任务类型，但没有遗物等级
     */
    private ChangeEvent createChangeEvent(Alert alert) {
        return ChangeEvent.of(
                SubscribeEnums.ALERTS,
                alert.getMissionInfo() != null ? alert.getMissionInfo().getMissionType() : null,
                null,  // 警报没有等级
                alert
        );
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.ALERTS;
    }
}
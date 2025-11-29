package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.enums.VoidEnum;
import io.github.kingprimes.model.worldstate.ActiveMission;
import io.github.kingprimes.model.worldstate.BastWorldState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 裂隙变化检测器
 * 检测新出现的虚空裂隙任务
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class FissuresChangeDetector implements ChangeDetector {

    @Override
    public List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState) {
        // 1. 边界检查
        if (newState == null || newState.getActiveMissions() == null) {
            log.debug("新状态或裂隙数据为空");
            return Collections.emptyList();
        }

        if (oldState == null || oldState.getActiveMissions() == null) {
            log.debug("旧状态为空，跳过裂隙检测");
            return Collections.emptyList();
        }

        // 2. 提取旧裂隙ID集合
        Set<BastWorldState.Id> oldMissionIds = oldState.getActiveMissions().stream()
                .map(ActiveMission::get_id)
                .collect(Collectors.toSet());

        // 3. 过滤出新增的裂隙
        List<ChangeEvent> changes = newState.getActiveMissions().stream()
                .filter(mission -> !oldMissionIds.contains(mission.get_id()))
                .map(this::createChangeEvent)
                .collect(Collectors.toList());

        if (!changes.isEmpty()) {
            log.info("检测到 {} 个新裂隙", changes.size());
        }

        return changes;
    }

    /**
     * 创建裂隙变化事件
     */
    private ChangeEvent createChangeEvent(ActiveMission mission) {
        return ChangeEvent.of(
                SubscribeEnums.FISSURES,
                mission.getMissionType(),  // 任务类型（捕获、生存等）
                parseTierNum(mission.getModifier()),  // 遗物等级（从modifier解析）
                mission                    // 完整任务数据
        );
    }

    /**
     * 从VoidEnum解析遗物等级
     * VoidT1 -> 1, VoidT2 -> 2, VoidT3 -> 3, VoidT4 -> 4, VoidT5 -> 5
     */
    private Integer parseTierNum(VoidEnum voidEnum) {
        if (voidEnum == null) {
            return null;
        }
        
        return switch (voidEnum) {
            case VoidT1 -> 1;
            case VoidT2 -> 2;
            case VoidT3 -> 3;
            case VoidT4 -> 4;
            case VoidT5 -> 5;
            case VoidT6 -> 6;
        };
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.FISSURES;
    }
}
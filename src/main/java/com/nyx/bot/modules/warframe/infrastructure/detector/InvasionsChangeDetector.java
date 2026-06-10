package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import io.github.kingprimes.model.worldstate.Invasion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 入侵变化检测器
 * 检测新出现的派系入侵事件
 */
@Slf4j
@Component
public class InvasionsChangeDetector implements ChangeDetector<Invasion> {
    private final WorldStateUtils worldStateUtils;

    public InvasionsChangeDetector(WorldStateUtils worldStateUtils) {
        this.worldStateUtils = worldStateUtils;
    }

    @Override
    public List<ChangeEvent<Invasion>> detectChanges(WorldState oldState, WorldState newState) {
        // 1. 边界检查
        if (newState == null || newState.getInvasions() == null) {
            log.debug("新状态或入侵数据为空");
            return Collections.emptyList();
        }

        if (oldState == null || oldState.getInvasions() == null) {
            log.debug("旧状态为空，跳过入侵检测");
            return Collections.emptyList();
        }

        // 2. 提取旧入侵ID集合
        Set<BastWorldState.Id> oldInvasionIds = oldState.getInvasions().stream()
                .map(Invasion::get_id)
                .collect(Collectors.toSet());

        // 3. 过滤出新增的入侵（且未完成的）
        List<ChangeEvent<Invasion>> changes = newState.getInvasions().stream()
                .filter(invasion -> !invasion.getCompleted())  // 只关注未完成的
                .filter(invasion -> !oldInvasionIds.contains(invasion.get_id()))
                .map(this::createChangeEvent)
                .collect(Collectors.toList());

        if (!changes.isEmpty()) {
            log.info("检测到 {} 个新入侵", changes.size());
        }

        return changes;
    }

    /**
     * 创建入侵变化事件，包含奖励类型用于后续过滤
     */
    private ChangeEvent<Invasion> createChangeEvent(Invasion invasion) {
        Invasion translated = worldStateUtils.translateInvasion(invasion);
        return ChangeEvent.of(
                SubscribeType.INVASIONS,
                null,
                null,
                InvasionReward.fromInvasion(translated),
                translated
        );
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.INVASIONS;
    }
}
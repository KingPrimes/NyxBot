package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import io.github.kingprimes.model.worldstate.Goal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventsChangeDetector implements ChangeDetector<Goal> {

    @Override
    public List<ChangeEvent<Goal>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getGoals() == null) {
            return Collections.emptyList();
        }
        if (oldState == null || oldState.getGoals() == null) {
            return Collections.emptyList();
        }
        Set<BastWorldState.Id> oldIds = oldState.getGoals().stream()
                .map(Goal::get_id)
                .collect(Collectors.toSet());
        List<ChangeEvent<Goal>> changes = newState.getGoals().stream()
                .filter(g -> !oldIds.contains(g.get_id()))
                .map(g -> ChangeEvent.of(SubscribeType.EVENTS, null, null, g))
                .toList();
        if (!changes.isEmpty()) {
            log.info("检测到 {} 个新活动", changes.size());
        }
        return changes;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.EVENTS;
    }
}

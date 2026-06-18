package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import io.github.kingprimes.model.worldstate.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NewsChangeDetector implements ChangeDetector<Event> {

    @Override
    public List<ChangeEvent<Event>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getEvents() == null) {
            return Collections.emptyList();
        }
        if (oldState == null || oldState.getEvents() == null) {
            return Collections.emptyList();
        }
        Set<BastWorldState.Id> oldIds = oldState.getEvents().stream()
                .map(Event::get_id)
                .collect(Collectors.toSet());
        List<ChangeEvent<Event>> changes = newState.getEvents().stream()
                .filter(e -> !oldIds.contains(e.get_id()))
                .map(e -> ChangeEvent.of(SubscribeType.NEWS, null, null, e))
                .toList();
        if (!changes.isEmpty()) {
            log.info("检测到 {} 条新新闻", changes.size());
        }
        return changes;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.NEWS;
    }
}

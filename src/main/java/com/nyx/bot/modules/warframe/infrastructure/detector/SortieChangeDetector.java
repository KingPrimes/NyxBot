package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import io.github.kingprimes.model.worldstate.Sortie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SortieChangeDetector implements ChangeDetector<Sortie> {

    @Override
    public List<ChangeEvent<Sortie>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getSorties() == null) {
            return Collections.emptyList();
        }
        if (oldState == null || oldState.getSorties() == null) {
            return Collections.emptyList();
        }
        Set<BastWorldState.Id> oldIds = oldState.getSorties().stream()
                .map(Sortie::get_id)
                .collect(Collectors.toSet());
        List<ChangeEvent<Sortie>> changes = newState.getSorties().stream()
                .filter(s -> !oldIds.contains(s.get_id()))
                .map(s -> ChangeEvent.of(SubscribeType.SORTIE, null, null, s))
                .toList();
        if (!changes.isEmpty()) {
            log.info("检测到 {} 个新突击", changes.size());
        }
        return changes;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.SORTIE;
    }
}

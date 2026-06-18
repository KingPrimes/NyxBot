package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.BastWorldState;
import io.github.kingprimes.model.worldstate.LiteSorite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ArchonHuntChangeDetector implements ChangeDetector<LiteSorite> {

    @Override
    public List<ChangeEvent<LiteSorite>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getLiteSorties() == null) {
            return Collections.emptyList();
        }
        if (oldState == null || oldState.getLiteSorties() == null) {
            return Collections.emptyList();
        }
        Set<BastWorldState.Id> oldIds = oldState.getLiteSorties().stream()
                .map(LiteSorite::get_id)
                .collect(Collectors.toSet());
        List<ChangeEvent<LiteSorite>> changes = newState.getLiteSorties().stream()
                .filter(s -> !oldIds.contains(s.get_id()))
                .map(s -> ChangeEvent.of(SubscribeType.ARCHON_HUNT, null, null, s))
                .toList();
        if (!changes.isEmpty()) {
            log.info("检测到执政官突击更新");
        }
        return changes;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.ARCHON_HUNT;
    }
}

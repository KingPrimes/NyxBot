package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.DuvalierCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class DuviriCycleChangeDetector implements ChangeDetector<DuvalierCycle> {
    private final WorldStateUtils worldStateUtils;

    public DuviriCycleChangeDetector(WorldStateUtils worldStateUtils) {
        this.worldStateUtils = worldStateUtils;
    }

    @Override
    public List<ChangeEvent<DuvalierCycle>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getDuvalierCycle() == null) {
            return Collections.emptyList();
        }
        DuvalierCycle cycle = newState.getDuvalierCycle();
        if (oldState != null && oldState.getDuvalierCycle() != null
                && cycle.getExpiry().equals(oldState.getDuvalierCycle().getExpiry())) {
            return Collections.emptyList();
        }
        log.info("双衍王境轮换已更新");
        return List.of(ChangeEvent.of(SubscribeType.DUVIRI_CYCLE, null, null, worldStateUtils.translateDuvalierCycle(cycle)));
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.DUVIRI_CYCLE;
    }
}

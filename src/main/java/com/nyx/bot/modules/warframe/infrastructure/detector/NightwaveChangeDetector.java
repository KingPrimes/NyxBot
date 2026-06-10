package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.worldstate.SeasonInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class NightwaveChangeDetector implements ChangeDetector<SeasonInfo> {

    @Override
    public List<ChangeEvent<SeasonInfo>> detectChanges(WorldState oldState, WorldState newState) {
        if (newState == null || newState.getSeasonInfo() == null) {
            return Collections.emptyList();
        }
        SeasonInfo info = newState.getSeasonInfo();
        if (oldState != null && oldState.getSeasonInfo() != null
                && info.getSeason() == oldState.getSeasonInfo().getSeason()) {
            return Collections.emptyList();
        }
        log.info("电波已更新");
        return List.of(ChangeEvent.of(SubscribeType.NIGHTWAVE, null, null, info));
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.NIGHTWAVE;
    }
}

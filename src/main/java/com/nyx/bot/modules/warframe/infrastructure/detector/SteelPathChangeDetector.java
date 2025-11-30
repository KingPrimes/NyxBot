package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.SteelPathOffering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 钢铁之路变化检测器
 * 检测钢铁之路奖励轮换
 */
@Slf4j
@Component
public class SteelPathChangeDetector implements ChangeDetector<SteelPathOffering> {

    @Override
    public List<ChangeEvent<SteelPathOffering>> detectChanges(WorldState oldState, WorldState newState) {
        // 边界检查
        if (newState == null || newState.getSteelPath() == null) {
            log.debug("新状态或钢铁之路数据为空");
            return Collections.emptyList();
        }

        if (oldState == null || oldState.getSteelPath() == null) {
            log.debug("旧状态为空，跳过钢铁之路检测");
            return Collections.emptyList();
        }

        SteelPathOffering oldSteelPath = oldState.getSteelPath();
        SteelPathOffering newSteelPath = newState.getSteelPath();

        // 检测奖励是否变化
        if (oldSteelPath.getNextReward() != null &&
                newSteelPath.getNextReward() != null &&
                !oldSteelPath.getNextReward().name().equals(newSteelPath.getNextReward().name())) {

            log.info("检测到钢铁之路奖励轮换 [旧:{}] [新:{}]",
                    oldSteelPath.getNextReward().name(),
                    newSteelPath.getNextReward().name());

            ChangeEvent<SteelPathOffering> event = ChangeEvent.of(
                    SubscribeEnums.STEEL_PATH,
                    null,  // 无任务类型
                    null,  // 无等级
                    newSteelPath
            );

            return List.of(event);
        }

        return Collections.emptyList();
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.STEEL_PATH;
    }
}
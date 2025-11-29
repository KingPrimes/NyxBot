package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.VoidTrader;
import io.github.kingprimes.model.worldstate.BastWorldState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 虚空商人变化检测器
 * <p>
 * 检测虚空商人Baro Ki'Teer的到来和离开，当商人状态变化时触发通知
 * </p>
 *
 * @author Nyx
 * @since 2024-01-01
 */
@Slf4j
@Component
public class VoidChangeDetector implements ChangeDetector {

    @Override
    public List<ChangeEvent> detectChanges(WorldState oldState, WorldState newState) {
        List<ChangeEvent> events = new ArrayList<>();

        // 边界检查
        if (newState == null || newState.getVoidTraders() == null || newState.getVoidTraders().isEmpty()) {
            log.debug("新状态或虚空商人数据为空");
            return events;
        }

        if (oldState == null || oldState.getVoidTraders() == null || oldState.getVoidTraders().isEmpty()) {
            log.debug("旧状态为空，返回所有虚空商人");
            // 首次加载，返回所有商人
            for (VoidTrader trader : newState.getVoidTraders()) {
                events.add(createChangeEvent(trader));
            }
            return events;
        }

        // 获取旧商人的ID集合
        List<BastWorldState.Id> oldTraderIds = oldState.getVoidTraders().stream()
                .map(VoidTrader::get_id)
                .toList();

        // 检测新增的商人
        for (VoidTrader newTrader : newState.getVoidTraders()) {
            BastWorldState.Id newTraderId = newTrader.get_id();
            
            if (!oldTraderIds.contains(newTraderId)) {
                log.info("检测到虚空商人变化 [位置:{}]", newTrader.getNode());
                events.add(createChangeEvent(newTrader));
            }
        }

        return events;
    }

    /**
     * 创建变化事件
     */
    private ChangeEvent createChangeEvent(VoidTrader trader) {
        return new ChangeEvent(
                SubscribeEnums.VOID,
                null,  // 无任务类型
                null,  // 无等级
                trader
        );
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.VOID;
    }
}
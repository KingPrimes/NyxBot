package com.nyx.bot.modules.warframe.infrastructure.detector;

import com.nyx.bot.modules.warframe.domain.service.ChangeDetector;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import io.github.kingprimes.model.WorldState;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.DailyDeals;
import io.github.kingprimes.model.worldstate.BastWorldState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 每日特惠变化检测器
 * <p>
 * 检测Darvo每日特惠的变化，当出现新的特惠商品时触发通知
 * </p>
 */
@Slf4j
@Component
public class DailyDealsChangeDetector implements ChangeDetector<DailyDeals> {

    @Override
    public List<ChangeEvent<DailyDeals>> detectChanges(WorldState oldState, WorldState newState) {
        List<ChangeEvent<DailyDeals>> events = new ArrayList<>();

        // 边界检查
        if (newState == null || newState.getDailyDeals() == null || newState.getDailyDeals().isEmpty()) {
            log.debug("新状态或每日特惠数据为空");
            return events;
        }

        if (oldState == null || oldState.getDailyDeals() == null || oldState.getDailyDeals().isEmpty()) {
            log.debug("旧状态为空，返回所有每日特惠");
            // 首次加载，返回所有特惠
            for (DailyDeals deal : newState.getDailyDeals()) {
                events.add(createChangeEvent(deal));
            }
            return events;
        }

        // 获取旧特惠的ID集合
        List<BastWorldState.Id> oldDealIds = oldState.getDailyDeals().stream()
                .map(DailyDeals::get_id)
                .toList();

        // 检测新增的特惠
        for (DailyDeals newDeal : newState.getDailyDeals()) {
            BastWorldState.Id newDealId = newDeal.get_id();
            
            if (!oldDealIds.contains(newDealId)) {
                log.info("检测到新的每日特惠 [商品:{}] [原价:{}]",
                        newDeal.getItem(),
                        newDeal.getOriginalPrice());
                
                events.add(createChangeEvent(newDeal));
            }
        }

        return events;
    }

    /**
     * 创建变化事件
     */
    private ChangeEvent<DailyDeals> createChangeEvent(DailyDeals deal) {
        return new ChangeEvent<>(
                SubscribeEnums.DAILY_DEALS,
                null,  // 无任务类型
                null,  // 无等级
                deal
        );
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.DAILY_DEALS;
    }
}
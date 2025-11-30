package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.Invasion;
import io.github.kingprimes.model.worldstate.Reward;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 入侵消息构建器
 * 构建派系入侵的通知消息
 */
@Slf4j
@Component
public class InvasionsMessageBuilder implements MessageBuilder<Invasion> {

    @Resource
    private WorldStateUtils worldStateUtils;

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Invasion> event, MissionSubscribeUserCheckType rule) {
        Invasion invasion = worldStateUtils.translateInvasion(event.getData());

        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        // 消息标题
        builder.text("\n━━━━━ 新入侵事件 ━━━━━");

        // 节点信息
        if (invasion.getNode() != null) {
            builder.text("\n📍 节点: " + invasion.getNode());
        }

        // 攻击方奖励
        if (invasion.getAttackerReward() != null && !invasion.getAttackerReward().isEmpty()) {
            builder.text("\n⚔️ 攻击方奖励:");
            invasion.getAttackerReward().forEach(reward -> {
                if (reward.getCountedItems() != null) {
                    reward.getCountedItems().forEach(item -> builder.text("\n  🎁 " + item.getName() + " x" + item.getCount()));
                }
            });
        }

        // 防守方奖励
        if (invasion.getDefenderReward() != null) {
            Reward defenderReward = invasion.getDefenderReward();
            if (defenderReward.getCountedItems() != null && !defenderReward.getCountedItems().isEmpty()) {
                builder.text("\n🛡️ 防守方奖励:");
                defenderReward.getCountedItems().forEach(item -> builder.text("\n  🎁 " + item.getName() + " x" + item.getCount()));
            }
        }

        // 完成度
        if (invasion.getCompletion() != null) {
            builder.text(String.format("\n📊 完成度: %s", invasion.getCompletion()));
        }

        builder.text("\n━━━━━━━━━━━━━━━━");

        return builder;
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.INVASIONS;
    }
}
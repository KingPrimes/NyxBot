package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.worldstate.Invasion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 入侵消息构建器
 * 构建派系入侵的通知消息
 */
@Slf4j
@Component
public class InvasionsMessageBuilder implements MessageBuilder<Invasion> {


    public InvasionsMessageBuilder() {
    }

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Invasion> event, MissionSubscribeUserCheckType rule) {
        Invasion invasion = event.data();
        String filterReward = rule != null && rule.getInvasionReward() != null
                ? rule.getInvasionReward().getName() : null;

        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        builder.text("\n━━━━━ 新入侵事件 ━━━━━");

        if (invasion.getNode() != null) {
            builder.text("\n📍 节点: " + invasion.getNode());
        }

        // 攻击方奖励
        if (invasion.getAttackerReward() != null && !invasion.getAttackerReward().isEmpty()) {
            builder.text("\n⚔️ 攻击方:");
            for (var reward : invasion.getAttackerReward()) {
                if (reward.getCountedItems() == null) continue;
                for (var item : reward.getCountedItems()) {
                    if (filterReward != null && !item.getName().contains(filterReward)) continue;
                    builder.text("\n  🎁 " + item.getName() + " x" + item.getCount());
                }
            }
        }

        // 防守方奖励
        if (invasion.getDefenderReward() != null) {
            var dr = invasion.getDefenderReward();
            if (dr.getCountedItems() != null && !dr.getCountedItems().isEmpty()) {
                builder.text("\n🛡️ 防守方:");
                for (var item : dr.getCountedItems()) {
                    if (filterReward != null && !item.getName().contains(filterReward)) continue;
                    builder.text("\n  🎁 " + item.getName() + " x" + item.getCount());
                }
            }
        }

        if (filterReward != null) {
            builder.text("\n🏷️ 已按 [" + filterReward + "] 过滤");
        }

        if (invasion.getCompletion() != null) {
            builder.text(String.format("\n📊 完成度: %s", invasion.getCompletion()));
        }

        builder.text("\n━━━━━━━━━━━━━━━━");
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.INVASIONS;
    }
}
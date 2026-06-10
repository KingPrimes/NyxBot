package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.worldstate.SteelPathOffering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 钢铁之路消息构建器
 * <p>
 * 负责构建钢铁之路奖励轮换的通知消息
 * </p>
 */
@Slf4j
@Component
public class SteelPathMessageBuilder implements MessageBuilder<SteelPathOffering> {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<SteelPathOffering> event, MissionSubscribeUserCheckType rule) {
        SteelPathOffering steelPath = event.data();

        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        // 消息标题
        builder.text("\n━━━━ 钢铁之路奖励轮换 ━━━━");

        // 当前奖励
        if (steelPath.getCurrentReward() != null) {
            builder.text("\n📦 当前奖励: " + steelPath.getCurrentReward().name());
        }

        // 下一个奖励
        if (steelPath.getNextReward() != null) {
            builder.text("\n✨ 下次奖励: " + steelPath.getNextReward().name());
        }

        // 剩余时间
        if (steelPath.getRemaining() != null && !steelPath.getRemaining().isEmpty()) {
            builder.text("\n⏰ 剩余时间: " + steelPath.getRemaining());
        }

        // 过期时间
        if (steelPath.getExpiry() != null) {
            builder.text("\n📅 过期时间: " + steelPath.getExpiry());
        }

        builder.text("\n━━━━━━━━━━━━━━━━");

        log.debug("构建钢铁之路消息完成");
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.STEEL_PATH;
    }
}
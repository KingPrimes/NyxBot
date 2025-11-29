package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.CetusCycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 夜灵平原周期消息构建器
 * 构建地球平原昼夜循环的通知消息
 *
 * @author Nyx Bot
 */
@Slf4j
@Component
public class CetusCycleMessageBuilder implements MessageBuilder {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent event, MissionSubscribeUserCheckType rule) {
        CetusCycle cycle = (CetusCycle) event.getData();

        ArrayMsgUtils builder = ArrayMsgUtils.builder();

        // 消息标题
        builder.text("\n━━━━━ 夜灵平原提醒 ━━━━━");

        // 当前状态
        builder.text("\n🌍 当前状态: " + cycle.getState());

        // 剩余时间
        if (cycle.getTimeLeft() != null && !cycle.getTimeLeft().isEmpty()) {
            builder.text("\n⏰ 剩余时间: " + cycle.getTimeLeft());
        }

        // 提示信息
        if (cycle.getCycle().equalsIgnoreCase("day")) {
            builder.text("\n💡 白天即将结束，夜晚即将来临");
        } else {
            builder.text("\n💡 夜晚即将结束，白天即将来临");
        }

        builder.text("\n━━━━━━━━━━━━━━━━");

        return builder;
    }

    @Override
    public SubscribeEnums getSupportedType() {
        return SubscribeEnums.CETUS_CYCLE;
    }
}
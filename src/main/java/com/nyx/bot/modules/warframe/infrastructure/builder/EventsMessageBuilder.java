package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.worldstate.Goal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventsMessageBuilder implements MessageBuilder<Goal> {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Goal> event, MissionSubscribeUserCheckType rule) {
        Goal goal = event.data();
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        builder.text("\n━━━━━ 新活动 ━━━━━");
        builder.text("\n🎯 " + goal.getDesc());
        if (goal.getNode() != null) {
            builder.text("\n📍 " + goal.getNode());
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.EVENTS;
    }
}

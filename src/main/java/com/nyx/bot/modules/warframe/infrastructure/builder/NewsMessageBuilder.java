package com.nyx.bot.modules.warframe.infrastructure.builder;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.modules.warframe.domain.service.MessageBuilder;
import com.nyx.bot.modules.warframe.domain.valueobject.ChangeEvent;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUserCheckType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import io.github.kingprimes.model.worldstate.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NewsMessageBuilder implements MessageBuilder<Event> {

    @Override
    public ArrayMsgUtils buildMessage(ChangeEvent<Event> event, MissionSubscribeUserCheckType rule) {
        Event news = event.data();
        ArrayMsgUtils builder = ArrayMsgUtils.builder();
        builder.text("\n━━━━━ Warframe 新闻 ━━━━━");
        if (news.getMessages() != null) {
            news.getMessages().forEach(m -> builder.text("\n📰 " + m.getMessage()));
        }
        if (news.getLinks() != null) {
            news.getLinks().forEach(l -> builder.text("\n🔗 " + l.getLink()));
        }
        return builder;
    }

    @Override
    public SubscribeType getSupportedType() {
        return SubscribeType.NEWS;
    }
}

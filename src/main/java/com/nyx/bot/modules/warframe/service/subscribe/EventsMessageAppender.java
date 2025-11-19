package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import io.github.kingprimes.model.enums.SubscribeEnums;
import org.springframework.stereotype.Component;

@Component
public class EventsMessageAppender implements MessageAppender {
    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums) {

    }
}

package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.modules.warframe.utils.WorldStateUtils;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class AlertsMessageAppender implements MessageAppender {

    @Resource
    WorldStateUtils utils;

    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums) throws DataNotInfoException {
        new SystemImage().addSystemImage(builder, enums);
    }
}

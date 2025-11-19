package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.common.exception.DataNotInfoException;
import io.github.kingprimes.model.enums.SubscribeEnums;
import org.springframework.stereotype.Component;

@Component
public class VoidMessageAppender implements MessageAppender {
    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums) throws DataNotInfoException {
        new SystemImage().addSystemImage(builder, enums);
    }
}

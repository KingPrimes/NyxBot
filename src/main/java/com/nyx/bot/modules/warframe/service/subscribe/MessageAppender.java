package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.common.exception.DataNotInfoException;
import io.github.kingprimes.model.enums.SubscribeEnums;


public interface MessageAppender {
    default void appendContent(ArrayMsgUtils builder, SubscribeEnums enums) throws DataNotInfoException {
    }
}

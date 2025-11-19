package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.cache.WarframeCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.utils.I18nUtils;
import io.github.kingprimes.model.enums.SubscribeEnums;
import io.github.kingprimes.model.worldstate.CetusCycle;
import org.springframework.stereotype.Component;

@Component
public class CetusCycleMessageAppender implements MessageAppender {
    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums) throws DataNotInfoException {
        CetusCycle data = WarframeCache.getWarframeStatus().getCetusCycle();
        builder.text(I18nUtils.message("warframe." + data.getCycle() + ".cetusCycle") + data.getTimeLeft());
    }
}

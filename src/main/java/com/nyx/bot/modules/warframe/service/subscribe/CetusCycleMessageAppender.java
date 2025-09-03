package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.I18nUtils;

public class CetusCycleMessageAppender implements MessageAppender {
    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        builder.text(I18nUtils.message("warframe." + data.getCetusCycle().getCycle() + ".cetusCycle") + data.getCetusCycle().getTimeLeft());
    }
}

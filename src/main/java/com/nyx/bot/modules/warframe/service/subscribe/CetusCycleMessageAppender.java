package com.nyx.bot.modules.warframe.service.subscribe;

import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.TimeUtils;
import com.nyx.bot.utils.onebot.Msg;

public class CetusCycleMessageAppender implements MessageAppender {
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        builder.text(I18nUtils.message("warframe." + data.getCetusCycle().getState() + ".cetusCycle") + TimeUtils.timeDeltaToNow(data.getCetusCycle().getExpiry().getEpochSecond()));
    }
}

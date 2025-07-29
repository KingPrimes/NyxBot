package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.TimeUtils;
import com.nyx.bot.utils.onebot.Msg;

public class CetusCycleMessageAppender implements MessageAppender {
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        builder.text(I18nUtils.message("warframe." + data.getCetusCycle().getState() + ".cetusCycle") + TimeUtils.timeDeltaToNow(data.getCetusCycle().getExpiry().getEpochSecond()));
    }
}

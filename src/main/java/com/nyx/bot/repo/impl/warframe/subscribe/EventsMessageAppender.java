package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.onebot.Msg;

public class EventsMessageAppender implements MessageAppender{
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        SystemImage.addSystemImage(builder, enums, subscribe, user, data.getEvents());
    }
}

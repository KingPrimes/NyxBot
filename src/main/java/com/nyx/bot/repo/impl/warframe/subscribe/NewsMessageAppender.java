package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.utils.onebot.Msg;

public class NewsMessageAppender implements MessageAppender{

    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        data.getEvents().forEach(n -> {

        });
    }
}

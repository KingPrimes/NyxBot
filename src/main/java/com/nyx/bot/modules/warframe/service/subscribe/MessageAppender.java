package com.nyx.bot.modules.warframe.service.subscribe;

import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.onebot.Msg;

public interface MessageAppender {
    default void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user){
        SystemImage.addSystemImage(builder, enums, subscribe, user, null);
    }
}

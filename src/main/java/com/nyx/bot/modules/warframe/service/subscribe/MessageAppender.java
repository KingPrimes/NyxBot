package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.res.WorldState;

public interface MessageAppender {
    default void appendContent(ArrayMsgUtils builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        SystemImage.addSystemImage(builder, enums, subscribe, user, null);
    }
}

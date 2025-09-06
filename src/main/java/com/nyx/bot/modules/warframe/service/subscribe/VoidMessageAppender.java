package com.nyx.bot.modules.warframe.service.subscribe;

import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.modules.warframe.entity.MissionSubscribe;
import com.nyx.bot.modules.warframe.entity.MissionSubscribeUser;
import com.nyx.bot.modules.warframe.res.WorldState;
import com.nyx.bot.utils.I18nUtils;

public class VoidMessageAppender implements MessageAppender {
    @Override
    public void appendContent(ArrayMsgUtils builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        if (data.getVoidTraders().get(0).getManifest().isEmpty()) {
            builder.text(I18nUtils.message("warframe.up.voidOut"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        } else {
            builder.text(I18nUtils.message("warframe.up.voidIn"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        }
    }
}

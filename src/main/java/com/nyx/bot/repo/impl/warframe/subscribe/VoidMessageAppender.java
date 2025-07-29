package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.WorldState;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.onebot.Msg;

public class VoidMessageAppender implements MessageAppender{
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, WorldState data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        if (data.getVoidTraders().get(0).getManifest().isEmpty()) {
            builder.text(I18nUtils.message("warframe.up.voidOut"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        } else {
            builder.text(I18nUtils.message("warframe.up.voidIn"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        }
    }
}

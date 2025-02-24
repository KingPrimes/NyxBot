package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.onebot.Msg;

public class VoidMessageAppender implements MessageAppender{
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        if (data.getVoidTrader().getInventory().isEmpty() && !data.getVoidTrader().getActive()) {
            builder.text(I18nUtils.message("warframe.up.voidOut"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        } else {
            builder.text(I18nUtils.message("warframe.up.voidIn"));
            SystemImage.addSystemImage(builder, enums, subscribe, user, null);
        }
    }
}

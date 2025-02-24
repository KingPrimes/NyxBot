package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.DateUtils;
import com.nyx.bot.utils.I18nUtils;
import com.nyx.bot.utils.onebot.Msg;

import java.util.Date;

public class CetusCycleMessageAppender implements MessageAppender {
    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        builder.text(I18nUtils.message("warframe." + data.getCetusCycle().getState() + ".cetusCycle") + DateUtils.getDiff(data.getCetusCycle().getExpiry(), new Date()));
    }
}

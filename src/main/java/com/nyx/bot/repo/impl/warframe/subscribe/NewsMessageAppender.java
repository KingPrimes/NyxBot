package com.nyx.bot.repo.impl.warframe.subscribe;

import com.nyx.bot.entity.warframe.MissionSubscribe;
import com.nyx.bot.entity.warframe.MissionSubscribeUser;
import com.nyx.bot.enums.SubscribeEnums;
import com.nyx.bot.res.GlobalStates;
import com.nyx.bot.utils.onebot.Msg;

public class NewsMessageAppender implements MessageAppender{

    @Override
    public void appendContent(Msg builder, SubscribeEnums enums, GlobalStates data, MissionSubscribe subscribe, MissionSubscribeUser user) {
        data.getNews().forEach(n -> {
            if (!n.getTranslations().getZh().isEmpty()) {
                builder.text(n.getTranslations().getZh());
            } else {
                builder.text(n.getTranslations().getEn());
            }
            builder.text("\n").img(n.getImageLink()).text(n.getLink()).text("\n");
        });
    }
}

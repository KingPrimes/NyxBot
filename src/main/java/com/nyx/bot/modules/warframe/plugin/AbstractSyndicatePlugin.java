package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.modules.warframe.utils.SyndicateMissionsUtils;
import com.nyx.bot.utils.onebot.SendUtils;
import io.github.kingprimes.model.enums.SyndicateEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractSyndicatePlugin {

    protected final SyndicateMissionsUtils syndicateMissionsUtils;

    protected AbstractSyndicatePlugin(SyndicateMissionsUtils syndicateMissionsUtils) {
        this.syndicateMissionsUtils = syndicateMissionsUtils;
    }

    protected void sendImage(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        SendUtils.send(bot, event,
                syndicateMissionsUtils.postSyndicateEntratiImage(getSyndicate()),
                getCode(), log);
    }

    protected abstract SyndicateEnum getSyndicate();

    protected abstract Codes getCode();
}

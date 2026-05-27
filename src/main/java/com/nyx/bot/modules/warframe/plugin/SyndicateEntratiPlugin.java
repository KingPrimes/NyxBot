package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.SyndicateMissionsUtils;
import io.github.kingprimes.model.enums.SyndicateEnum;
import org.springframework.stereotype.Component;

/**
 * 英择谛 赏金
 */
@Shiro
@Component
public class SyndicateEntratiPlugin extends AbstractSyndicatePlugin {

    public SyndicateEntratiPlugin(SyndicateMissionsUtils syndicateMissionsUtils) {
        super(syndicateMissionsUtils);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SYNDICATE_ENTRATI_CMD, at = AtEnum.BOTH)
    public void syndicateEntranceHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        sendImage(bot, event);
    }

    @Override
    protected SyndicateEnum getSyndicate() {
        return SyndicateEnum.EntratiSyndicate;
    }

    @Override
    protected Codes getCode() {
        return Codes.WARFRAME_SYNDICATE_ENTRATI;
    }
}

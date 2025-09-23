package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.res.enums.SyndicateEnum;
import com.nyx.bot.modules.warframe.utils.SyndicateMissionsUtils;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 索拉里斯 赏金
 */
@Shiro
@Component
@Slf4j
public class SyndicateSolarisUnitedPlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SYNDICATE_SOLARIS_UNITED_CMD,at = AtEnum.BOTH)
    public void syndicateSolarisUnitedHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, SyndicateMissionsUtils.postSyndicateEntratiImage(SyndicateEnum.SolarisSyndicate), Codes.WARFRAME_SYNDICATE_OSTRONS, log);
    }
}

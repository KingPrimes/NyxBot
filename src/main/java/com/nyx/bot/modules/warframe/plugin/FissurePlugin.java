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
import com.nyx.bot.modules.warframe.res.worldstate.ActiveMission;
import com.nyx.bot.modules.warframe.utils.FissuresUtils;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 裂隙
 */
@Shiro
@Component
@Slf4j
public class FissurePlugin {
    /**
     * 裂隙
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_CMD,at = AtEnum.BOTH)
    public void activeMissionHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postFissuresImage(0), Codes.WARFRAME_ACTIVE_MISSION_PLUGIN, log);
    }

    /**
     * 钢铁裂隙
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ACTIVE_MISSION_PATH_CMD,at = AtEnum.BOTH)
    public void activeMissionPathHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postFissuresImage(2), Codes.WARFRAME_ACTIVE_MISSION_PATH_PLUGIN, log);
    }

    /**
     * 虚空风暴
     */

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_VOID_STORMS_CMD,at = AtEnum.BOTH)
    public void steelPathHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postFissuresImage(1), Codes.WARFRAME_VOID_STORMS_PLUGIN, log);
    }

    private byte[] postFissuresImage(Integer type) throws DataNotInfoException, HtmlToImageException {
        List<ActiveMission> list = FissuresUtils.getFissures(type);
        return HtmlToImage.generateImage("html/fissues", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("type", type);
            modelMap.put("fissues", list);
            return modelMap;
        }).toByteArray();
    }
}

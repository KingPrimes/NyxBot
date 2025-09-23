package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.cache.ArbitrationCache;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

/**
 * 仲裁
 */
@Shiro
@Component
@Slf4j
public class ArbitrationPlugin {

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_CMD,at = AtEnum.BOTH)
    public void arbitrationHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, arbitrationImage(), Codes.WARFRAME_ARBITRATION_PLUGIN, log);
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_ARBITRATION_EX_CMD,at = AtEnum.BOTH)
    public void arbitrationExHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, postArbitrationExImage(), Codes.WARFRAME_ARBITRATION_EX_PLUGIN, log);
    }


    private byte[] arbitrationImage() throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/arbitration", () -> {
            ModelMap modelMap = new ModelMap();
            try {
                modelMap.put("arbit", ArbitrationCache.getArbitration().orElseThrow(() -> new DataNotInfoException("仲裁信息不存在")));
            } catch (DataNotInfoException e) {
                throw new RuntimeException(e);
            }
            return modelMap;
        }).toByteArray();
    }

    private byte[] postArbitrationExImage() throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/arbitration_ex", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("arbitrations", ArbitrationCache.getArbitrationList());
            return modelMap;
        }).toByteArray();
    }
}

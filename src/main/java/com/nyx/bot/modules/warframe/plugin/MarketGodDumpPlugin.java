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
import com.nyx.bot.modules.warframe.res.Ducats;
import com.nyx.bot.modules.warframe.utils.MarketUtils;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 查询 Market市场中的金垃圾
 */
@Shiro
@Component
@Slf4j
public class MarketGodDumpPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_MARKET_GOD_DUMP_CMD,at = AtEnum.BOTH)
    public void marketGodDump(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        Ducats ducats = MarketUtils.getDucats();
        if (Objects.isNull(ducats)) {
            log.debug("获取ducat失败");
            bot.sendMsg(event, "获取ducat失败", false);
            return;
        }
        Map<String, List<Ducats.Ducat>> godDump = Objects.requireNonNull(ducats).getPayload().getGodDump();
        byte[] bytes = postMarketDucatsImage(godDump);
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_MARKET_GOD_DUMP, log);
    }

    private byte[] postMarketDucatsImage(Map<String, List<Ducats.Ducat>> map) throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/marketDucats", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("code", true);
            modelMap.put("data", map);
            return modelMap;
        }).toByteArray();
    }
}

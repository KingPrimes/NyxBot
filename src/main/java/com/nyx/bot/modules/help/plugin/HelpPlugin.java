package com.nyx.bot.modules.help.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.HashMap;
import java.util.Map;

@Shiro
@Component
@Slf4j
public class HelpPlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.HELP_CMD)
    public void helpHandler(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, getHelpImage(), Codes.HELP, log);
        bot.sendMsg(event, ArrayMsgUtils.builder()
                .text("指令使用方法请查看以下文档：https://kingprimes.top/posts/1bb16eb")
                .build(), false);

    }

    private byte[] getHelpImage() throws DataNotInfoException, HtmlToImageException {
        Map<String, String> v = new HashMap<>();
        for (Codes value : Codes.values()) {
            v.put(StringUtils.removeMatcher(value.getComm()), value.getPermissions().getStr());
        }
        return HtmlToImage.generateImage("html/help", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("codes", v);
            return modelMap;
        }).toByteArray();
    }
}

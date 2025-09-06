package com.nyx.bot.modules.system.plugin;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.SystemInfoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.net.UnknownHostException;

@Shiro
@Component
@Slf4j
public class SystemInfoPlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.CHECK_VERSION_CMD)
    public void systemInfoHandler(Bot bot, AnyMessageEvent event) throws UnknownHostException, DataNotInfoException, HtmlToImageException {
        SendUtils.send(bot, event, getSystemInfoImage(), Codes.CHECK_VERSION, log);

    }

    private byte[] getSystemInfoImage() throws UnknownHostException, DataNotInfoException, HtmlToImageException {
        JSONObject info = SystemInfoUtils.getInfo();
        return HtmlToImage.generateImage("html/systemInfo", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.addAttribute("info", info);
            return modelMap;
        }).toByteArray();
    }

}

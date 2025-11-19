package com.nyx.bot.modules.system.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.utils.SendUtils;
import com.nyx.bot.utils.SystemInfoUtils;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.AllInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Shiro
@Component
@Slf4j
public class SystemInfoPlugin {

    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.CHECK_VERSION_CMD, at = AtEnum.BOTH)
    public void systemInfoHandler(Bot bot, AnyMessageEvent event) throws UnknownHostException {
        SendUtils.send(bot, event, getSystemInfoImage(), Codes.CHECK_VERSION, log);
    }

    private byte[] getSystemInfoImage() throws UnknownHostException {
        AllInfo info = SystemInfoUtils.getInfo();
        return drawImagePlugin.drawAllInfoImage(info);
    }

}

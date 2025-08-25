package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.nyx.bot.common.core.OneBotLogInfoData;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.enums.HttpCodeEnum;
import com.nyx.bot.modules.warframe.utils.RivenAttributeCompute;
import com.nyx.bot.utils.http.HttpUtils;
import com.nyx.bot.utils.onebot.ImageUrlUtils;
import com.nyx.bot.utils.onebot.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 紫卡分析
 */
@Shiro
@Component
@Slf4j
public class RivenAnalysePlugin {
    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_ANALYSE_CMD)
    public void rivenAnalyse(Bot bot, AnyMessageEvent event) {
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        if (msgImgUrlList.isEmpty()) {
            bot.sendMsg(event, "请在指令后方添加上您要查询的紫卡图片!", false);
            return;
        }
        if (msgImgUrlList.size() > 5) {
            bot.sendMsg(event, "查询紫卡图片一次性不可大于5张", false);
            return;
        }
        OneBotLogInfoData data = WarframeSend.getLogInfoData(bot, event, Codes.WARFRAME_RIVEN_ANALYSE);
        data.setData(RivenAttributeCompute.ocrRivenCompute(event));
        HttpUtils.Body body = ImageUrlUtils.builderBase64Post("postRivenAnalyseImage", data);
        if (body.getCode().equals(HttpCodeEnum.SUCCESS)) {
            bot.sendMsg(event,
                    Msg.builder().imgBase64(body.getFile()).build(), false);
        } else {
            WarframeSend.sendErrorMsg(bot, event, body);
        }
    }
}

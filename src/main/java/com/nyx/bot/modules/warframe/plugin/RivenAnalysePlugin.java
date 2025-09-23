package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.common.exception.HtmlToImageException;
import com.nyx.bot.enums.Codes;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.core.RivenAnalyseTrendModel;
import com.nyx.bot.modules.warframe.utils.RivenAttributeCompute;
import com.nyx.bot.utils.HtmlToImage;
import com.nyx.bot.utils.SendUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import java.util.List;

/**
 * 紫卡分析
 */
@Shiro
@Component
@Slf4j
public class RivenAnalysePlugin {


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_RIVEN_ANALYSE_CMD, at = AtEnum.BOTH)
    public void rivenAnalyse(Bot bot, AnyMessageEvent event) throws DataNotInfoException, HtmlToImageException {
        List<String> msgImgUrlList = ShiroUtils.getMsgImgUrlList(event.getArrayMsg());
        if (msgImgUrlList.isEmpty()) {
            bot.sendMsg(event, "请在指令后方添加上您要查询的紫卡图片!", false);
            return;
        }
        if (msgImgUrlList.size() > 5) {
            bot.sendMsg(event, "查询紫卡图片一次性不可大于5张", false);
            return;
        }
        byte[] bytes = postRivenAnalyseImage(RivenAttributeCompute.ocrRivenCompute(event));
        SendUtils.send(bot, event, bytes, Codes.WARFRAME_RIVEN_ANALYSE, log);
    }

    private byte[] postRivenAnalyseImage(List<List<RivenAnalyseTrendModel>> lists) throws DataNotInfoException, HtmlToImageException {
        return HtmlToImage.generateImage("html/rivenAnalyseTrend", () -> {
            ModelMap modelMap = new ModelMap();
            modelMap.put("data", lists);
            return modelMap;
        }).toByteArray();
    }
}

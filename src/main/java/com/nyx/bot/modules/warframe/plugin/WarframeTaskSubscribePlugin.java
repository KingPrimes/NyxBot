package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.common.exception.DataNotInfoException;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.utils.WarframeSubscribeCheck;
import io.github.kingprimes.DrawImagePlugin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Warframe 任务订阅
 */
@Shiro
@Component
@Slf4j
public class WarframeTaskSubscribePlugin {


    @Resource
    DrawImagePlugin drawImagePlugin;

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SUBSCRIBE_CMD, at = AtEnum.BOTH)
    public void subscribe(Bot bot, AnyMessageEvent event) throws DataNotInfoException {
        if (!ActionParams.GROUP.equals(event.getMessageType())) {
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }
        String str = event.getRawMessage().replace("订阅", "").replace(" ", "").trim();

        if (str.isEmpty()) {
            bot.sendMsg(event,
                    ArrayMsgUtils.builder().img(postSubscribeHelp()).build(), false);
            return;
        }

        String ms = new WarframeSubscribeCheck().userSubscriptions(bot.getSelfId(),
                event.getUserId(),
                bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), false).getData().getNickname(),
                event.getGroupId(),
                bot.getGroupInfo(event.getGroupId(), false).getData().getGroupName(),
                str);

        bot.sendMsg(event, ms, false);
    }


    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_UNSUBSCRIBE_CMD)
    public void unsubscribe(Bot bot, AnyMessageEvent event) {
        if (!ActionParams.GROUP.equals(event.getMessageType())) {
            bot.sendMsg(event, "此指令只能在群组中使用！", false);
            return;
        }
        String str = event.getRawMessage().replace("取消订阅", "").replace(" ", "").trim();

        if (str.isEmpty()) {
            bot.sendMsg(event,
                    ArrayMsgUtils.builder().img(postSubscribeHelp()).build(), false);
            return;
        }

        String ms = new WarframeSubscribeCheck().userCancelSubscribe(
                event.getUserId(),
                event.getGroupId(),
                str);
        bot.sendMsg(event, ms, false);
    }


    private byte[] postSubscribeHelp() {
        return drawImagePlugin.drawWarframeSubscribeImage(WarframeSubscribeCheck.getSubscribeEnums(), WarframeSubscribeCheck.getSubscribeMissionTypeEnums());
    }
}

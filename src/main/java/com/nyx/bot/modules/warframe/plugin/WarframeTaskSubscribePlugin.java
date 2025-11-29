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
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.domain.valueobject.SubscriptionCommand;
import io.github.kingprimes.DrawImagePlugin;
import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Warframe 任务订阅
 */
@Shiro
@Component
@Slf4j
public class WarframeTaskSubscribePlugin {


    @Resource
    DrawImagePlugin drawImagePlugin;

    @Resource
    SubscriptionApplicationService subscriptionService;

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

        // 解析订阅参数
        SubscriptionCommand command = parseSubscriptionCommand(
                bot.getSelfId(),
                event.getUserId(),
                bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), false).getData().getNickname(),
                event.getGroupId(),
                bot.getGroupInfo(event.getGroupId(), false).getData().getGroupName(),
                str
        );

        if (command == null) {
            bot.sendMsg(event, "参数错误，格式：订阅类型[必填]-任务类型[可选]-遗物等级[可选]\n" +
                    "示例：订阅9 或 订阅9-2 或 订阅9-2-4", false);
            return;
        }

        // 使用新服务处理订阅
        String result = subscriptionService.subscribe(command);
        bot.sendMsg(event, result, false);
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

        // 解析取消订阅参数
        String[] parts = str.split("-");
        try {
            SubscribeEnums subscribeType = parseSubscribeType(parts[0]);
            MissionTypeEnum missionType = parts.length > 1 && !parts[1].isEmpty() ?
                    parseMissionType(parts[1]) : null;
            Integer tier = parts.length > 2 && !parts[2].isEmpty() ?
                    Integer.parseInt(parts[2]) : null;

            // 使用新服务处理取消订阅
            String result = subscriptionService.unsubscribe(
                    event.getGroupId(),
                    event.getUserId(),
                    subscribeType,
                    missionType,
                    tier
            );
            bot.sendMsg(event, result, false);
        } catch (Exception e) {
            bot.sendMsg(event, "参数错误，格式：取消订阅类型[必填]-任务类型[可选]-遗物等级[可选]", false);
        }
    }

    /**
     * 解析订阅命令
     */
    private SubscriptionCommand parseSubscriptionCommand(
            Long botUid, Long userId, String userName,
            Long groupId, String groupName, String raw
    ) {
        String[] parts = raw.split("-");
        if (parts.length < 1 || parts[0].isEmpty()) {
            return null;
        }

        try {
            SubscribeEnums subscribeType = parseSubscribeType(parts[0]);
            MissionTypeEnum missionType = parts.length > 1 && !parts[1].isEmpty() ?
                    parseMissionType(parts[1]) : null;
            Integer tier = parts.length > 2 && !parts[2].isEmpty() ?
                    Integer.parseInt(parts[2]) : null;

            return SubscriptionCommand.builder()
                    .botUid(botUid)
                    .groupId(groupId)
                    .groupName(groupName)
                    .userId(userId)
                    .userName(userName)
                    .subscribeType(subscribeType)
                    .missionType(missionType)
                    .tier(tier)
                    .build();
        } catch (Exception e) {
            log.error("解析订阅参数失败: {}", raw, e);
            return null;
        }
    }

    /**
     * 解析订阅类型
     */
    private SubscribeEnums parseSubscribeType(String input) {
        int code = Integer.parseInt(input);
        if (code <= 0 || code >= SubscribeEnums.values().length) {
            return SubscribeEnums.ERROR;
        }
        return SubscribeEnums.values()[code];
    }

    /**
     * 解析任务类型
     */
    private MissionTypeEnum parseMissionType(String input) {
        int code = Integer.parseInt(input);
        if (code <= 0 || code >= MissionTypeEnum.values().length) {
            return MissionTypeEnum.MT_RELAY;
        }
        return MissionTypeEnum.values()[code];
    }

    private byte[] postSubscribeHelp() {
        return drawImagePlugin.drawWarframeSubscribeImage(
                getSubscribeEnums(),
                getSubscribeMissionTypeEnums()
        );
    }

    /**
     * 获取订阅枚举属性，枚举变Map
     */
    private Map<Integer, String> getSubscribeEnums() {
        return Arrays.stream(SubscribeEnums.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, SubscribeEnums::getNAME));
    }

    /**
     * 获取任务类型枚举属性，枚举变Map
     */
    private Map<Integer, String> getSubscribeMissionTypeEnums() {
        return Arrays.stream(MissionTypeEnum.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, MissionTypeEnum::getName));
    }
}

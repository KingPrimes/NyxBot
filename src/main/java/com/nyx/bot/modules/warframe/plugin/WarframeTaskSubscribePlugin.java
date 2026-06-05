package com.nyx.bot.modules.warframe.plugin;

import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.GroupDecreaseHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupDecreaseNoticeEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.nyx.bot.enums.CommandConstants;
import com.nyx.bot.modules.warframe.application.SubscriptionApplicationService;
import com.nyx.bot.modules.warframe.domain.valueobject.SubscriptionCommand;
import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.modules.warframe.enums.MissionType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;
import com.nyx.bot.modules.warframe.repo.subscribe.MissionSubscribeRepository;
import io.github.kingprimes.DrawImagePlugin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Warframe 任务订阅
 */
@Shiro
@Component
@Slf4j
public class WarframeTaskSubscribePlugin {

    private final DrawImagePlugin drawImagePlugin;
    private final SubscriptionApplicationService subscriptionService;
    private final MissionSubscribeRepository subscribeRepo;
    private final Executor taskExecutor;

    public WarframeTaskSubscribePlugin(DrawImagePlugin drawImagePlugin,
                                       SubscriptionApplicationService subscriptionService,
                                       MissionSubscribeRepository subscribeRepo,
                                       Executor taskExecutor) {
        this.drawImagePlugin = drawImagePlugin;
        this.subscriptionService = subscriptionService;
        this.subscribeRepo = subscribeRepo;
        this.taskExecutor = taskExecutor;
    }

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = CommandConstants.WARFRAME_SUBSCRIBE_CMD, at = AtEnum.BOTH)
    public void subscribe(Bot bot, AnyMessageEvent event) {
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

        // 并行获取群成员昵称和群名（3s 超时 + NPE fallback）
        CompletableFuture<String> nicknameFuture = CompletableFuture.supplyAsync(
                () -> safeGetNickname(bot, event), taskExecutor);
        CompletableFuture<String> groupNameFuture = CompletableFuture.supplyAsync(
                () -> safeGetGroupName(bot, event), taskExecutor);

        String nickname = nicknameFuture.completeOnTimeout(
                String.valueOf(event.getUserId()), 3, TimeUnit.SECONDS).join();
        String groupName = groupNameFuture.completeOnTimeout(
                String.valueOf(event.getGroupId()), 3, TimeUnit.SECONDS).join();

        SubscriptionCommand command = parseSubscriptionCommand(
                bot.getSelfId(),
                event.getUserId(),
                nickname,
                event.getGroupId(),
                groupName,
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
            String subs = subscriptionService.getUserSubscriptionInfo(
                    event.getGroupId(), event.getUserId());
            if (!subs.isEmpty()) {
                bot.sendMsg(event, subs + "\n发送「取消订阅+类型编号」取消指定订阅\n" +
                        "例：取消订阅9 或 取消订阅9-2", false);
            } else {
                bot.sendMsg(event, "您还没有任何订阅，发送「订阅」查看可订阅的内容", false);
            }
            return;
        }

        try {
            String[] parts = str.split("-");
            SubscribeType subscribeType = parseSubscribeType(parts[0]);
            var params = parseSubscribeParams(parts, subscribeType);

            String result = subscriptionService.unsubscribe(
                    event.getGroupId(),
                    event.getUserId(),
                    subscribeType,
                    params.missionType,
                    params.tier,
                    params.invasionReward
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
            SubscribeType subscribeType = parseSubscribeType(parts[0]);
            var params = parseSubscribeParams(parts, subscribeType);

            return new SubscriptionCommand(botUid, groupId, groupName, userId, userName,
                    subscribeType, params.missionType, params.tier, params.invasionReward);
        } catch (Exception e) {
            log.error("解析订阅参数失败: {}", raw, e);
            return null;
        }
    }

    /**
     * 根据订阅类型解析后续参数
     */
    private ParsedParams parseSubscribeParams(String[] parts, SubscribeType subscribeType) {
        MissionType missionType = null;
        Integer tier = null;
        InvasionReward invasionReward = null;

        switch (subscribeType) {
            case INVASIONS -> {
                invasionReward = parts.length > 1 && !parts[1].isEmpty() ?
                        parseInvasionReward(parts[1]) : null;
            }
            case FISSURES -> {
                missionType = parts.length > 1 && !parts[1].isEmpty() ?
                        parseMissionType(parts[1]) : null;
                tier = parts.length > 2 && !parts[2].isEmpty() ?
                        Integer.parseInt(parts[2]) : null;
            }
            case ARBITRATION -> {
                missionType = parts.length > 1 && !parts[1].isEmpty() ?
                        parseMissionType(parts[1]) : null;
            }
            default -> {
                // 无需额外参数
            }
        }
        return new ParsedParams(missionType, tier, invasionReward);
    }

    /**
     * 解析订阅类型
     */
    private SubscribeType parseSubscribeType(String input) {
        int code = Integer.parseInt(input);
        if (code <= 0 || code >= SubscribeType.values().length) {
            throw new IllegalArgumentException("无效的订阅类型: " + code);
        }
        return SubscribeType.values()[code];
    }

    /**
     * 解析任务类型
     */
    private MissionType parseMissionType(String input) {
        int code = Integer.parseInt(input);
        if (code <= 0 || code >= MissionType.values().length) {
            throw new IllegalArgumentException("无效的任务类型: " + code);
        }
        return MissionType.values()[code];
    }

    /**
     * 解析入侵奖励
     */
    private InvasionReward parseInvasionReward(String input) {
        int code = Integer.parseInt(input);
        if (code <= 0 || code >= InvasionReward.values().length) {
            throw new IllegalArgumentException("无效的入侵奖励: " + code);
        }
        return InvasionReward.values()[code];
    }

    /**
     * 安全获取群成员昵称，API 异常或返回 null 时 fallback 到 userId
     */
    private String safeGetNickname(Bot bot, AnyMessageEvent event) {
        try {
            var data = bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), false).getData();
            return data != null ? data.getNickname() : String.valueOf(event.getUserId());
        } catch (Exception e) {
            log.warn("获取群成员信息失败 userId={}", event.getUserId(), e);
            return String.valueOf(event.getUserId());
        }
    }

    /**
     * 安全获取群名，API 异常或返回 null 时 fallback 到 groupId
     */
    private String safeGetGroupName(Bot bot, AnyMessageEvent event) {
        try {
            var data = bot.getGroupInfo(event.getGroupId(), false).getData();
            return data != null ? data.getGroupName() : String.valueOf(event.getGroupId());
        } catch (Exception e) {
            log.warn("获取群信息失败 groupId={}", event.getGroupId(), e);
            return String.valueOf(event.getGroupId());
        }
    }

    private byte[] postSubscribeHelp() {
        return drawImagePlugin.drawWarframeSubscribeImage(
                getSubscribeEnums(),
                getSubscribeMissionTypeEnums(),
                getInvasionRewardEnums()
        );
    }

    /**
     * 获取订阅枚举属性，枚举变Map
     */
    private Map<Integer, String> getSubscribeEnums() {
        return Arrays.stream(SubscribeType.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, SubscribeType::getName));
    }

    /**
     * 获取任务类型枚举属性，枚举变Map
     */
    private Map<Integer, String> getSubscribeMissionTypeEnums() {
        return Arrays.stream(MissionType.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, MissionType::getName));
    }

    /**
     * 获取入侵奖励枚举属性，枚举变Map
     */
    private Map<Integer, String> getInvasionRewardEnums() {
        return Arrays.stream(InvasionReward.values())
                .filter(e -> e.ordinal() != 0)
                .collect(Collectors.toMap(Enum::ordinal, InvasionReward::getName));
    }

    /**
     * 监听群成员减少事件：用户退群时删除其订阅数据，Bot 被踢时删除整个群的订阅
     */
    @GroupDecreaseHandler
    public void onGroupDecrease(Bot bot, GroupDecreaseNoticeEvent event) {
        try {
            boolean isBotKicked = event.getUserId() == bot.getSelfId();
            if (isBotKicked) {
                subscribeRepo.findBySubGroup(event.getGroupId())
                        .ifPresent(subscribeRepo::delete);
                log.info("Bot 被移出群 [group:{}]，已清除所有订阅数据", event.getGroupId());
            } else {
                subscribeRepo.findBySubGroup(event.getGroupId())
                        .ifPresent(sub -> {
                            sub.getUsers().removeIf(u -> u.getUserId().equals(event.getUserId()));
                            subscribeRepo.save(sub);
                        });
                log.info("用户退群 [group:{}] [user:{}]，已清除其订阅数据", event.getGroupId(), event.getUserId());
            }
        } catch (Exception e) {
            log.error("处理退群事件失败 [group:{}] [user:{}]", event.getGroupId(), event.getUserId(), e);
        }
    }

    private record ParsedParams(MissionType missionType, Integer tier, InvasionReward invasionReward) {
    }
}

package com.nyx.bot.modules.warframe.domain.valueobject;

import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import lombok.Builder;
import lombok.Value;

/**
 * 订阅命令对象
 * 封装用户订阅请求的所有参数
 */
@Value
@Builder
public class SubscriptionCommand {
    /**
     * Bot UID
     */
    Long botUid;

    /**
     * 群组 ID
     */
    Long groupId;

    /**
     * 群组名称
     */
    String groupName;

    /**
     * 用户 ID
     */
    Long userId;

    /**
     * 用户名称
     */
    String userName;

    /**
     * 订阅类型
     */
    SubscribeEnums subscribeType;

    /**
     * 任务类型（可选，null表示全部）
     */
    MissionTypeEnum missionType;

    /**
     * 遗物等级（可选，null表示全部）
     */
    Integer tier;
}
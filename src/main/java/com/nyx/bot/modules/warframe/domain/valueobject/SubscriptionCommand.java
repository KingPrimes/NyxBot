package com.nyx.bot.modules.warframe.domain.valueobject;

import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.modules.warframe.enums.MissionType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;

/**
 * 订阅命令对象
 * 封装用户订阅请求的所有参数
 *
 * @param botUid         Bot UID
 * @param groupId        群组 ID
 * @param groupName      群组名称
 * @param userId         用户 ID
 * @param userName       用户名称
 * @param subscribeType  订阅类型
 * @param missionType    任务类型（可选，null表示全部）
 * @param tier           遗物等级（可选，null表示全部）
 * @param invasionReward 入侵奖励物品（可选，仅 INVASIONS 类型使用，null表示全部）
 */
public record SubscriptionCommand(Long botUid, Long groupId, String groupName, Long userId, String userName,
                                  SubscribeType subscribeType, MissionType missionType, Integer tier,
                                  InvasionReward invasionReward) {
}

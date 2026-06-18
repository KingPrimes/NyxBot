package com.nyx.bot.modules.warframe.domain.valueobject;

import com.nyx.bot.modules.warframe.enums.InvasionReward;
import com.nyx.bot.modules.warframe.enums.MissionType;
import com.nyx.bot.modules.warframe.enums.SubscribeType;

/**
 * 变化事件值对象
 * 用于封装检测到的游戏状态变化
 *
 * @param type           订阅类型
 * @param missionType    任务类型（可选，null表示不限）
 * @param tier           遗物等级（可选，null表示不限）
 * @param invasionReward 入侵奖励物品（可选，仅 INVASIONS 类型使用，null表示不限）
 * @param data           变化的具体数据
 */
public record ChangeEvent<T>(SubscribeType type, MissionType missionType, Integer tier,
                             InvasionReward invasionReward, T data) {

    public static <T> ChangeEvent<T> of(SubscribeType type, MissionType missionType, Integer tier, T data) {
        return new ChangeEvent<>(type, missionType, tier, null, data);
    }

    public static <T> ChangeEvent<T> of(SubscribeType type, MissionType missionType, Integer tier,
                                        InvasionReward reward, T data) {
        return new ChangeEvent<>(type, missionType, tier, reward, data);
    }

    public static <T> ChangeEvent<T> of(SubscribeType type, T data) {
        return new ChangeEvent<>(type, null, null, null, data);
    }
}

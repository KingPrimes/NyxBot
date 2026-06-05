package com.nyx.bot.modules.warframe.enums;

import lombok.Getter;

/**
 * 入侵奖励物品枚举
 * 用于入侵订阅的物品过滤
 *
 * @author KingPrimes
 */
@Getter
public enum InvasionReward {

    NONE("无"),
    DETONITE_INJECTOR("突变原聚合物"),
    FIELDRON("力场装置样本"),
    MUTAGEN_MASS("诱变剂物质"),
    OROKIN_CATALYST("Orokin 催化剂"),
    OROKIN_REACTOR("Orokin 反应堆"),
    FORMA("Forma"),
    EXILUS_ADAPTER("特殊功能槽连接器"),
    ;

    private final String name;

    InvasionReward(String name) {
        this.name = name;
    }

    /**
     * 从已翻译的入侵数据中识别奖励类型
     */
    public static InvasionReward fromInvasion(io.github.kingprimes.model.worldstate.Invasion invasion) {
        if (invasion == null) return NONE;
        InvasionReward result = NONE;
        // 检查防守方奖励
        if (invasion.getDefenderReward() != null
                && invasion.getDefenderReward().getCountedItems() != null) {
            for (var item : invasion.getDefenderReward().getCountedItems()) {
                InvasionReward r = matchReward(item.getName());
                if (r != NONE) result = r;
            }
        }
        // 检查攻击方奖励
        if (result == NONE && invasion.getAttackerReward() != null) {
            for (var reward : invasion.getAttackerReward()) {
                if (reward.getCountedItems() != null) {
                    for (var item : reward.getCountedItems()) {
                        InvasionReward r = matchReward(item.getName());
                        if (r != NONE) return r;
                    }
                }
            }
        }
        return result;
    }

    private static InvasionReward matchReward(String itemName) {
        if (itemName == null) return NONE;
        for (InvasionReward r : values()) {
            if (r == NONE) continue;
            if (itemName.contains(r.name)) return r;
        }
        return NONE;
    }
}

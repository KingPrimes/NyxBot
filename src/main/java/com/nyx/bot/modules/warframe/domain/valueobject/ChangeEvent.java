package com.nyx.bot.modules.warframe.domain.valueobject;

import io.github.kingprimes.model.enums.MissionTypeEnum;
import io.github.kingprimes.model.enums.SubscribeEnums;
import lombok.Value;

/**
 * 变化事件值对象
 * 用于封装检测到的游戏状态变化
 */
@Value
public class ChangeEvent {
    /**
     * 订阅类型
     */
    SubscribeEnums type;

    /**
     * 任务类型（可选，null表示不限）
     */
    MissionTypeEnum missionType;

    /**
     * 遗物等级（可选，null表示不限）
     */
    Integer tier;

    /**
     * 变化的具体数据
     */
    Object data;

    /**
     * 创建变化事件
     *
     * @param type        订阅类型
     * @param missionType 任务类型（可null）
     * @param tier        遗物等级（可null）
     * @param data        具体数据
     * @return 变化事件
     */
    public static ChangeEvent of(SubscribeEnums type, MissionTypeEnum missionType, Integer tier, Object data) {
        return new ChangeEvent(type, missionType, tier, data);
    }

    /**
     * 创建变化事件（无任务类型和等级）
     *
     * @param type 订阅类型
     * @param data 具体数据
     * @return 变化事件
     */
    public static ChangeEvent of(SubscribeEnums type, Object data) {
        return new ChangeEvent(type, null, null, data);
    }
}
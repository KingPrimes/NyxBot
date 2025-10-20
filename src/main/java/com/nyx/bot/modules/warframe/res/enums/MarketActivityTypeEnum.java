package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

/**
 * Market 用户活动状态
 */
@Getter
public enum MarketActivityTypeEnum {
    /**
     * 未知
     */
    UNKNOWN("未知"),
    /**
     * 空闲
     */
    IDLE("空闲"),
    /**
     * 任务中
     */
    ON_MISSION("任务中"),
    /**
     * 在道场
     */
    IN_DOJO("在道场"),
    /**
     * 在轨道器
     */
    IN_ORBITER("在轨道器"),
    /**
     * 在中继站
     */
    IN_RELAY("在中继站");


    private final String activityType;

    MarketActivityTypeEnum(String activityType) {
        this.activityType = activityType;
    }
}

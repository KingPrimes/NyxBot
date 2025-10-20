package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

/**
 * Market 用户在线状态
 */
@Getter
public enum MarketStatusEnum {
    /**
     * 不可见
     */
    INVISIBLE("不可见"),
    /**
     * 离线
     */
    OFFLINE("离线"),
    /**
     * 在线
     */
    ONLINE("在线"),
    /**
     * 游戏中
     */
    INGAME("游戏中"),

    ;

    private final String status;

    MarketStatusEnum(String status) {
        this.status = status;
    }
}

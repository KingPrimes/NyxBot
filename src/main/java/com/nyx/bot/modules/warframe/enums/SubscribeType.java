package com.nyx.bot.modules.warframe.enums;

import lombok.Getter;

/**
 * 订阅类型枚举
 *
 * @author KingPrimes
 */
@Getter
public enum SubscribeType {

    ALERTS("警报"),
    ARBITRATION("仲裁"),
    CETUS_CYCLE("夜灵平野"),
    DAILY_DEALS("每日特惠"),
    EVENTS("活动"),
    INVASIONS("入侵"),
    STEEL_PATH("钢铁兑换"),
    VOID("奸商"),
    FISSURES("裂隙"),
    NEWS("新闻"),
    NIGHTWAVE("电波"),
    SORTIE("突击"),
    ARCHON_HUNT("执政官突击"),
    DUVIRI_CYCLE("双衍王境"),
    ;

    private final String name;

    SubscribeType(String name) {
        this.name = name;
    }
}

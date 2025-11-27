package com.nyx.bot.modules.warframe.enums;

import lombok.Getter;

@Getter
public enum MarketSortBy {
    /**
     * 价格正序
     */
    PRICE_ASC("price_asc"),
    /**
     * 价格倒序
     */
    PRICE_DESC("price_desc"),
    /**
     * 伤害正序
     */
    DAMAGE_ASC("damage_asc"),
    /**
     * 伤害倒序
     */
    DAMAGE_DESC("damage_desc"),
    ;
    private final String value;

    MarketSortBy(String value) {
        this.value = value;
    }
}

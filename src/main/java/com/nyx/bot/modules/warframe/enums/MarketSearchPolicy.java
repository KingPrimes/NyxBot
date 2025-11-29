package com.nyx.bot.modules.warframe.enums;

import lombok.Getter;

@Getter
public enum MarketSearchPolicy {
    /**
     * 全部
     */
    ANY("any"),
    /**
     * 售卖
     */
    DIRECT("direct"),
    // 拍卖
    /**
     * 拍卖
     */
    AUCTION("auction"),
    ;
    private final String value;

    MarketSearchPolicy(String value) {
        this.value = value;
    }
}
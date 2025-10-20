package com.nyx.bot.modules.warframe.res.enums;

import lombok.Getter;

/**
 * 交易状态
 */
@Getter
public enum TransactionEnum {
    SELL("sell"),
    BUY("buy"),
    ALL("all"),
    NONE("none"),
    ;

    private final String name;

    TransactionEnum(String name) {
        this.name = name;
    }
}
